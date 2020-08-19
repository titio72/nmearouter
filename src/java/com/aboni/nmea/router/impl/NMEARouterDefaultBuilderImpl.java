/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAFilterable;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.NMEARouterBuilder;
import com.aboni.nmea.router.agent.AgentStatusManager;
import com.aboni.nmea.router.agent.AgentStatusManager.STATUS;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEAAgentBuilderJson;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.agent.impl.DepthStatsAgent;
import com.aboni.nmea.router.agent.impl.NMEA2FileAgent;
import com.aboni.nmea.router.agent.impl.NMEAAutoPilotAgent;
import com.aboni.nmea.router.agent.impl.QOSKeys;
import com.aboni.nmea.router.agent.impl.system.*;
import com.aboni.nmea.router.conf.ConfJSON;
import com.aboni.nmea.router.conf.LogLevelType;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.filters.impl.JSONFilterSetSerializer;
import com.aboni.nmea.router.processors.NMEASourcePriorityProcessor;
import com.aboni.utils.ServerLog;
import com.aboni.utils.ThingsFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("OverlyCoupledClass")
public class NMEARouterDefaultBuilderImpl implements NMEARouterBuilder {

    private static final boolean ENABLE_GPS_TIME = true;
    private static final boolean ENABLE_AP = false;
    private final AgentStatusManager agentStatusManager;

    private NMEARouter theRouter;

    @Inject
    public NMEARouterDefaultBuilderImpl(AgentStatusManager agentStatusManager) {
        this.agentStatusManager = agentStatusManager;
    }

    @Override
    public void init(NMEARouter router, Properties unused) {
        try {
            theRouter = router;
            buildRouter();
        } catch (MalformedConfigurationException e) {
            Logger.getGlobal().log(Level.SEVERE, "Error", e);
        }
    }

    private void buildRouter() throws MalformedConfigurationException {
        NMEARouter r = theRouter;
        LogLevelType logLevel;
        List<String> gpsPriority;

        ConfJSON cJ;
        try {
            cJ = new ConfJSON();
            logLevel = cJ.getLogLevel();
            gpsPriority = cJ.getGPSPriority();
        } catch (Exception e) {
            throw new MalformedConfigurationException("Cannot read configuration", e);
        }

        configureGPSPriority(gpsPriority, r);
        configureLog(logLevel);
        if (ENABLE_GPS_TIME) buildGPSTimeTarget(r);
        buildStreamDump(r);
        buildPowerLedTarget(r);
        buildFanTarget(r);
        buildDPTStats(r);
        buildEngineDetector(r);
        buildWebUI(r);
        if (ENABLE_AP) buildAutoPilot(r);

        buildAgents(cJ, r);
    }

    private void buildAgents(ConfJSON conf, NMEARouter r) throws MalformedConfigurationException {
        NMEAAgentBuilderJson builder = ThingsFactory.getInstance(NMEAAgentBuilderJson.class);
        for (ConfJSON.AgentDef a : conf.getAgents()) {
            NMEAAgent agent = builder.createAgent(a);
            if (agent != null) {
                r.addAgent(agent);
                handlePersistentState(agent);
            }
        }
    }

    private void configureLog(LogLevelType level) {
        switch (level) {
            case DEBUG:
                ServerLog.getLoggerAdmin().setDebug();
                break;
            case WARNING:
                ServerLog.getLoggerAdmin().setWarning();
                break;
            case ERROR:
                ServerLog.getLoggerAdmin().setError();
                break;
            case NONE:
                ServerLog.getLoggerAdmin().setNone();
                break;
            default:
                ServerLog.getLoggerAdmin().setInfo();
                break;
        }
    }

    private void configureGPSPriority(List<String> gpsPriority, NMEARouter r) {
        if (gpsPriority != null && !gpsPriority.isEmpty()) {
            NMEASourcePriorityProcessor processor = new NMEASourcePriorityProcessor(ThingsFactory.getInstance(NMEACache.class));
            processor.addAllGPS();
            for (int i = 0; i < gpsPriority.size(); i++) {
                processor.setPriority(gpsPriority.get(i), gpsPriority.size() - i /* first has the highest priority */);
            }
            r.addProcessor(processor);
        }
    }

    private void handlePersistentState(NMEAAgent agent) {
        boolean activate = handleActivation(agent);
        handleFilter(agent);
        if (activate) agent.start();
    }

    private void handleFilter(NMEAAgent agent) {
        NMEAFilterable tgt = agent.getTarget();
        if (tgt != null) {
            String data = agentStatusManager.getFilterOutData(agent.getName());
            if (data == null) {
                agentStatusManager.setFilterOutData(agent.getName(), new JSONFilterSetSerializer().exportFilter(tgt.getFilter()));
            } else {
                tgt.setFilter(new JSONFilterSetSerializer().importFilter(data));
            }
        }
        NMEAFilterable src = agent.getSource();
        if (src != null) {
            String data = agentStatusManager.getFilterInData(agent.getName());
            if (data == null) {
                agentStatusManager.setFilterInData(agent.getName(), new JSONFilterSetSerializer().exportFilter(src.getFilter()));
            } else {
                src.setFilter(new JSONFilterSetSerializer().importFilter(data));
            }
        }
    }

    private boolean handleActivation(NMEAAgent agent) {
        boolean active = false;
        if (agentStatusManager != null) {
            AgentStatusManager.STATUS requestedStatus = agentStatusManager.getStartMode(agent.getName());
            if (requestedStatus == STATUS.UNKNOWN) {
                agentStatusManager.setStartMode(agent.getName(), STATUS.AUTO);
            } else {
                active = (requestedStatus == STATUS.AUTO);
            }
        }
        return active;
    }

    private void buildStreamDump(NMEARouter r) {
        QOS q = createBuiltInQOS();
        NMEA2FileAgent dumper = ThingsFactory.getInstance(NMEA2FileAgent.class);
        dumper.setup("Log", q);
        r.addAgent(dumper);
        handlePersistentState(dumper);
        handleFilter(dumper);
    }

    private void buildDPTStats(NMEARouter r) {
        QOS q = createBuiltInQOS();
        DepthStatsAgent a = ThingsFactory.getInstance(DepthStatsAgent.class);
        a.setup("Depth", q);
        r.addAgent(a);
        a.start();
    }

    private void buildPowerLedTarget(NMEARouter r) {
        if (System.getProperty("os.arch").startsWith("arm")) {
            QOS q = createBuiltInQOS();
            q.addProp(QOSKeys.CANNOT_START_STOP);
            PowerLedAgent pwrLed = ThingsFactory.getInstance(PowerLedAgent.class);
            pwrLed.setup("PowerLed", q);
            r.addAgent(pwrLed);
            pwrLed.start();
        }
    }

    private void buildAutoPilot(NMEARouter r) {
        QOS q = createBuiltInQOS();
        NMEAAutoPilotAgent ap = ThingsFactory.getInstance(NMEAAutoPilotAgent.class);
        ap.setup("SmartPilot", q);
        r.addAgent(ap);
        ap.start();
    }

    private void buildFanTarget(NMEARouter r) {
        QOS q = createBuiltInQOS();
        q.addProp(QOSKeys.CANNOT_START_STOP);
        FanAgent fan = ThingsFactory.getInstance(FanAgent.class);
        fan.setup("FanManager", q);
        r.addAgent(fan);
        fan.start();
    }

    private void buildEngineDetector(NMEARouter r) {
        QOS q = createBuiltInQOS();
        q.addProp(QOSKeys.CANNOT_START_STOP);
        EngineDetectionAgent eng = ThingsFactory.getInstance(EngineDetectionAgent.class);
        eng.setup("EngineManager", q);
        r.addAgent(eng);
        eng.start();
    }

    private void buildGPSTimeTarget(NMEARouter r) {
        QOS q = createBuiltInQOS();
        q.addProp(QOSKeys.CANNOT_START_STOP);
        NMEASystemTimeGPS gpsTime = ThingsFactory.getInstance(NMEASystemTimeGPS.class);
        gpsTime.setup("GPSTime", q);
        r.addAgent(gpsTime);
        handleFilter(gpsTime);
        gpsTime.start();
    }

    private void buildWebUI(NMEARouter r) {
        QOS q = createBuiltInQOS();
        q.addProp(QOSKeys.CANNOT_START_STOP);
        WebInterfaceAgent web = ThingsFactory.getInstance(WebInterfaceAgent.class);
        web.setup("UI", q);
        r.addAgent(web);
        web.start();
    }

    private QOS createBuiltInQOS() {
        QOS q = new QOS();
        q.addProp(QOSKeys.BUILT_IN);
        return q;
    }
}
