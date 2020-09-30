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

import com.aboni.nmea.router.*;
import com.aboni.nmea.router.agent.AgentStatusManager;
import com.aboni.nmea.router.agent.AgentStatusManager.STATUS;
import com.aboni.nmea.router.agent.BuiltInAgents;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEAAgentBuilderJson;
import com.aboni.nmea.router.conf.AgentConfJSON;
import com.aboni.nmea.router.conf.ConfJSON;
import com.aboni.nmea.router.conf.LogLevelType;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.filters.FilterSetSerializer;
import com.aboni.nmea.router.processors.NMEASourcePriorityProcessor;
import com.aboni.utils.ServerLog;
import com.aboni.utils.ThingsFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NMEARouterDefaultBuilderImpl implements NMEARouterBuilder {

    private static final boolean ENABLE_GPS_TIME = true;
    private static final boolean ENABLE_AP = false;
    private final AgentStatusManager agentStatusManager;
    private final NMEAAgentBuilderJson builder;
    private final FilterSetSerializer filterSetSerializer;

    private NMEARouter theRouter;

    @Inject
    public NMEARouterDefaultBuilderImpl(@NotNull AgentStatusManager agentStatusManager, @NotNull NMEAAgentBuilderJson builder,
                                        @NotNull @Named(Constants.TAG_JSON) FilterSetSerializer filterSetSerializer) {
        this.agentStatusManager = agentStatusManager;
        this.builder = builder;
        this.filterSetSerializer = filterSetSerializer;
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
        Set<NMEAAgent> toActivate = new HashSet<>();
        for (AgentConfJSON a : conf.getAgents()) {
            NMEAAgent agent = builder.createAgent(a);
            if (agent != null) {
                r.addAgent(agent);
                handleFilter(agent);
                if (handleActivation(agent)) toActivate.add(agent);
            }
        }
        for (NMEAAgent a : toActivate) {
            a.start();
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

    private void handleFilter(NMEAAgent agent) {
        NMEAFilterable tgt = agent.getTarget();
        if (tgt != null) {
            String data = agentStatusManager.getFilterOutData(agent.getName());
            if (data == null) {
                agentStatusManager.setFilterOutData(agent.getName(), filterSetSerializer.exportFilter(tgt.getFilter()));
            } else {
                tgt.setFilter(filterSetSerializer.importFilter(data));
            }
        }
        NMEAFilterable src = agent.getSource();
        if (src != null) {
            String data = agentStatusManager.getFilterInData(agent.getName());
            if (data == null) {
                agentStatusManager.setFilterInData(agent.getName(), filterSetSerializer.exportFilter(src.getFilter()));
            } else {
                src.setFilter(filterSetSerializer.importFilter(data));
            }
        }
    }

    private boolean handleActivation(NMEAAgent agent) {
        boolean active = false;
        if (agentStatusManager != null) {
            AgentStatusManager.STATUS requestedStatus = agentStatusManager.getStartMode(agent.getName());
            if (requestedStatus == STATUS.UNKNOWN) {
                agentStatusManager.setStartMode(agent.getName(), STATUS.MANUAL);
            } else {
                active = (requestedStatus == STATUS.AUTO);
            }
        }
        return active;
    }

    private void buildStreamDump(NMEARouter r) {
        NMEAAgent dumper = builder.createAgent(BuiltInAgents.FILE_DUMPER);
        if (dumper != null) {
            r.addAgent(dumper);
            handleFilter(dumper);
        }
    }

    private void buildDPTStats(NMEARouter r) {
        NMEAAgent a = builder.createAgent(BuiltInAgents.DEPTH_STATS);
        if (a != null) {
            r.addAgent(a);
            a.start();
        }
    }

    private void buildPowerLedTarget(NMEARouter r) {
        NMEAAgent pwrLed = builder.createAgent(BuiltInAgents.POWER_LED);
        if (pwrLed != null) {
            r.addAgent(pwrLed);
            pwrLed.start();
        }
    }

    private void buildAutoPilot(NMEARouter r) {
        NMEAAgent ap = builder.createAgent(BuiltInAgents.AUTO_PILOT);
        if (ap != null) {
            r.addAgent(ap);
            ap.start();
        }
    }

    private void buildFanTarget(NMEARouter r) {
        NMEAAgent fan = builder.createAgent(BuiltInAgents.FAN_MANAGER);
        if (fan != null) {
            r.addAgent(fan);
            fan.start();
        }
    }

    private void buildEngineDetector(NMEARouter r) {
        NMEAAgent eng = builder.createAgent(BuiltInAgents.ENGINE_DETECTOR);
        if (eng != null) {
            r.addAgent(eng);
            eng.start();
        }
    }

    private void buildGPSTimeTarget(NMEARouter r) {
        NMEAAgent gpsTime = builder.createAgent(BuiltInAgents.GPS_TIME_SYNC);
        if (gpsTime != null) {
            r.addAgent(gpsTime);
            handleFilter(gpsTime);
            gpsTime.start();
        }
    }

    private void buildWebUI(NMEARouter r) {
        NMEAAgent web = builder.createAgent(BuiltInAgents.WEB_UI);
        if (web != null) {
            r.addAgent(web);
            web.start();
        }
    }
}
