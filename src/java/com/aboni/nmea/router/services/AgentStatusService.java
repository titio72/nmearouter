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
import com.aboni.nmea.router.agent.AgentStatusManager;
import com.aboni.nmea.router.agent.AgentStatusManager.STATUS;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.services.impl.AgentListSerializer;
import com.aboni.nmea.router.utils.Log;
import org.json.JSONObject;

import javax.inject.Inject;
import java.time.Instant;

public class AgentStatusService extends JSONWebService {

    private final NMEARouter router;
    private final AgentStatusManager agentStatusManager;

    @Inject
    public AgentStatusService(NMEARouter router, AgentStatusManager agentStatusManager, Log log) {
        super(log);
        if (router==null) throw new IllegalArgumentException("router is null");
        if (agentStatusManager==null) throw new IllegalArgumentException("Agent status manager is null");
        this.router = router;
        this.agentStatusManager = agentStatusManager;
        setLoader((ServiceConfig config) -> {
            try {
                String msg = doActivate(config);
                JSONObject res = new AgentListSerializer(agentStatusManager).getJSON(router, msg);
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
                    agentStatusManager.setStartMode(agent, "1".equals(auto) ? STATUS.AUTO : STATUS.MANUAL);
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
