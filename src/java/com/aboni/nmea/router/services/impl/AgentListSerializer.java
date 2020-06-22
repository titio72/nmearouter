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

package com.aboni.nmea.router.services.impl;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.AgentStatusManager;
import com.aboni.nmea.router.agent.AgentStatusManager.STATUS;
import com.aboni.nmea.router.agent.NMEAAgent;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;

public class AgentListSerializer {

    private final AgentStatusManager agentStatusManager;

    @Inject
    public AgentListSerializer(AgentStatusManager agentStatusManager) {
        this.agentStatusManager = agentStatusManager;
    }

    public JSONObject getJSON(NMEARouter router, String message) {
        JSONObject res = new JSONObject();
        res.put("message", message);
        JSONArray servicesJSON = new JSONArray();
        res.put("agents", servicesJSON);
        for (String agentName : router.getAgents()) {
            NMEAAgent ag = router.getAgent(agentName);
            boolean auto = agentStatusManager.getStartMode(ag.getName()) == STATUS.AUTO;
            String outFilter = agentStatusManager.getFilterOutData(ag.getName());
            String inFilter = agentStatusManager.getFilterInData(ag.getName());
            outFilter = (outFilter != null && outFilter.isEmpty()) ? null : outFilter;
            inFilter = (inFilter != null && inFilter.isEmpty()) ? null : inFilter;

            JSONObject agJSON = new JSONObject();
            agJSON.put("agent", ag.getName());
            agJSON.put("description", ag.getDescription());
            agJSON.put("type", ag.getType());
            agJSON.put("started", ag.isStarted());
            agJSON.put("source", (ag.getSource() != null));
            agJSON.put("target", (ag.getTarget() != null));
            agJSON.put("startStop", ag.isUserCanStartAndStop());
            agJSON.put("builtin", ag.isBuiltIn());
            agJSON.put("auto", auto);
            agJSON.put("hasFilterIn", (inFilter != null));
            if (inFilter != null) agJSON.put("filterIn", new JSONObject(inFilter));
            agJSON.put("hasFilterOut", (outFilter != null));
            if (outFilter != null) agJSON.put("filterOut", new JSONObject(outFilter));
            servicesJSON.put(agJSON);
        }
        return res;
    }
}