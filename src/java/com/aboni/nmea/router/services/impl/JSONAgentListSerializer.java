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

import com.aboni.nmea.router.agent.AgentPersistentStatus;
import com.aboni.nmea.router.agent.AgentPersistentStatusManager;
import com.aboni.nmea.router.agent.AgentActivationMode;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.filters.NMEAFilter;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.Collection;

public class JSONAgentListSerializer {

    private final AgentPersistentStatusManager agentStatusManager;

    @Inject
    public JSONAgentListSerializer(AgentPersistentStatusManager agentStatusManager) {
        if (agentStatusManager==null) throw new IllegalArgumentException("Agent status manager is null");
        this.agentStatusManager = agentStatusManager;
    }

    public JSONObject getJSON(Collection<NMEAAgent> agents, String message) {
        JSONObject res = new JSONObject();
        res.put("message", message);
        JSONArray servicesJSON = new JSONArray();
        res.put("agents", servicesJSON);
        for (NMEAAgent ag: agents) {
            JSONObject agJSON = ag.toJSON();
            AgentPersistentStatus agentPersistentStatus = agentStatusManager.getPersistentStatus(ag.getName());
            if (agentPersistentStatus!=null) {
                boolean auto = agentPersistentStatus.getStatus() == AgentActivationMode.AUTO;
                NMEAFilter tgtFilter = agentPersistentStatus.getTargetFilter();
                NMEAFilter srcFilter = agentPersistentStatus.getSourceFilter();
                agJSON.put("auto", auto);
                agJSON.put("conf_filterSource", srcFilter.toJSON());
                agJSON.put("conf_filterTarget", tgtFilter.toJSON());
            }
            servicesJSON.put(agJSON);
        }
        return res;
    }
}