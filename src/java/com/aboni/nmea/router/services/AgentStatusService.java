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

import com.aboni.log.Log;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.AgentActivationMode;
import com.aboni.nmea.router.agent.AgentPersistentStatusManager;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.filters.JSONFilterParser;

import javax.inject.Inject;

public class AgentStatusService extends JSONWebService {

    @Inject
    public AgentStatusService(NMEARouter router, AgentPersistentStatusManager agentStatusManager, JSONFilterParser filterSerializer, Log log) {
        super(log);
        if (filterSerializer==null) throw new IllegalArgumentException("Filter serializer is null");
        setLoader(new AgentServiceHelper(router, agentStatusManager) {
            @Override
            protected String execute(ServiceConfig config) throws ServiceException {
                return executeService(getRouter(), getAgentStatusManager(), config);
            }
        });
    }

    protected String executeService(NMEARouter router, AgentPersistentStatusManager statusManager, ServiceConfig config) {
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
                    statusManager.setStartMode(agent, "1".equals(auto) ? AgentActivationMode.AUTO : AgentActivationMode.MANUAL);
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
