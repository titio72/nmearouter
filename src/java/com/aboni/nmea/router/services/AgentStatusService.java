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

package com.aboni.nmea.router.services;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.AgentPersistentStatusManager;
import com.aboni.nmea.router.agent.AgentActivationMode;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.filters.JSONFilterParser;
import com.aboni.nmea.router.services.impl.JSONAgentListSerializer;
import com.aboni.log.Log;
import org.json.JSONObject;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AgentStatusService extends JSONWebService {

    private final NMEARouter router;
    private final AgentPersistentStatusManager agentStatusManager;

    @Inject
    public AgentStatusService(NMEARouter router, AgentPersistentStatusManager agentStatusManager, JSONFilterParser filterSerializer, Log log) {
        super(log);
        if (router==null) throw new IllegalArgumentException("router is null");
        if (agentStatusManager==null) throw new IllegalArgumentException("Agent status manager is null");
        if (filterSerializer==null) throw new IllegalArgumentException("Filter serializer is null");
        this.router = router;
        this.agentStatusManager = agentStatusManager;
        setLoader((ServiceConfig config) -> {
            try {
                String msg = doActivate(config);
                List<NMEAAgent> agents = new ArrayList<>();
                for (String agName: router.getAgents()) {
                    NMEAAgent a = router.getAgent(agName);
                    if (a!=null) agents.add(a);
                }

                JSONObject res = new JSONAgentListSerializer(agentStatusManager).getJSON(agents, msg);
                res.put("time", Instant.now().toString());
                return res;
            } catch (Exception e) {
                throw new JSONGenerationException(e);
            }
        });
    }

    private String doActivate(ServiceConfig config) {
        String msg = "";
        String agent = config.getParameter("agent");
        String auto = config.getParameter("auto");
        String active = config.getParameter("active");
        if (agent!=null) {
            NMEAAgent a = router.getAgent(agent);
            if (a!=null) {
                if (active!=null) {
                    msg = startStopService(a, active);
                }

                if (auto!=null) {
                    agentStatusManager.setStartMode(agent, "1".equals(auto) ? AgentActivationMode.AUTO : AgentActivationMode.MANUAL);
                }
            } else {
                msg = "Cannot change status of agent '" + agent + "'. Agent unknown.";
            }
        }
        return msg;
    }

    private static String startStopService(NMEAAgent a, String activate) {
        String msg;
        if (a.isUserCanStartAndStop()) {
            if ("YES".equalsIgnoreCase(activate) || "1".equals(activate)) {
                msg = activate(a);
            } else if ("NO".equalsIgnoreCase(activate) || "0".equals(activate)) {
                msg = deactivate(a);
            } else {
                msg = "Unknown status '" + activate + "'";
            }
        } else {
            msg = "This agent does not support starting/stopping";
        }
        return msg;
    }

    private static String deactivate(NMEAAgent a) {
        String msg;
        if (a.isStarted()) {
            a.stop();
            msg = getMessage(a, "stopped");
        } else {
            msg = getMessage(a, "not started");
        }
        return msg;
    }

    private static String activate(NMEAAgent a) {
        String msg;
        if (a.isStarted()) {
            msg = getMessage(a, "already started");
        } else {
            a.start();
            msg = getMessage(a, "started");
        }
        return msg;
    }

    private static String getMessage(NMEAAgent a, String msg) {
        return "Agent '" + a.getName() + "' " + msg;
    }
}
