/*
 * Copyright (c) 2023,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.services;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.AgentPersistentStatus;
import com.aboni.nmea.router.agent.AgentPersistentStatusManager;
import com.aboni.nmea.router.agent.NMEAAgent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AgentServiceHelper implements WebServiceJSONLoader {

    private final AgentPersistentStatusManager agentStatusManager;
    private final NMEARouter router;

    protected AgentServiceHelper(NMEARouter router, AgentPersistentStatusManager agentStatusManager) {
        if (agentStatusManager == null) throw new IllegalArgumentException("Agent status manager is null");
        if (router == null) throw new IllegalArgumentException("Router manager is null");
        this.agentStatusManager = agentStatusManager;
        this.router = router;
    }

    @Override
    public JSONObject getResult(ServiceConfig config) throws JSONGenerationException {
        try {
            String messageToReturn = execute(config);
            List<NMEAAgent> agents = new ArrayList<>();
            for (String ag : router.getAgents()) {
                NMEAAgent a = router.getAgent(ag);
                if (a != null) agents.add(a);
            }
            return getJSON(agents, messageToReturn);
        } catch (ServiceException e) {
            throw new JSONGenerationException(e);
        }
    }


    protected NMEARouter getRouter() {
        return router;
    }

    protected AgentPersistentStatusManager getAgentStatusManager() {
        return agentStatusManager;
    }

    protected abstract String execute(ServiceConfig config) throws ServiceException;

    protected JSONObject getJSON(Collection<NMEAAgent> agents, String message) {
        JSONObject res = new JSONObject();
        res.put("message", message);
        res.put("time", Instant.now().toString());
        JSONArray servicesJSON = new JSONArray();
        res.put("agents", servicesJSON);
        for (NMEAAgent ag : agents) {
            JSONObject agJSON = ag.toJSON();
            AgentPersistentStatus agentPersistentStatus = agentStatusManager.getPersistentStatus(ag.getName());
            if (agentPersistentStatus != null) {
                agJSON.put("configuration", agentPersistentStatus.toJSON());
            }
            servicesJSON.put(agJSON);
        }
        return res;
    }
}
