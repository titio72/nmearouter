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
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.filters.FilterSetSerializer;
import com.aboni.nmea.router.processors.NMEASourcePriorityProcessor;
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
        List<String> gpsPriority;

        ConfJSON cJ;
        try {
            cJ = new ConfJSON();
            gpsPriority = cJ.getGPSPriority();
        } catch (Exception e) {
            throw new MalformedConfigurationException(e);
        }

        configureGPSPriority(gpsPriority, r);
        if (ENABLE_GPS_TIME) buildBuiltInAgent(r, BuiltInAgents.GPS_TIME_SYNC, true);
        if (ENABLE_AP) buildBuiltInAgent(r, BuiltInAgents.AUTO_PILOT, false);
        buildBuiltInAgent(r, BuiltInAgents.FILE_DUMPER, false);
        buildBuiltInAgent(r, BuiltInAgents.POWER_LED, true);
        buildBuiltInAgent(r, BuiltInAgents.FAN_MANAGER, true);
        buildBuiltInAgent(r, BuiltInAgents.DEPTH_STATS, true);
        buildBuiltInAgent(r, BuiltInAgents.ENGINE_DETECTOR, true);
        buildBuiltInAgent(r, BuiltInAgents.WEB_UI, true);

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

    private void buildBuiltInAgent(NMEARouter r, BuiltInAgents a, boolean doStart) {
        NMEAAgent agent = builder.createAgent(a);
        if (agent != null) {
            r.addAgent(agent);
            if (doStart) agent.start();
        }
    }
}
