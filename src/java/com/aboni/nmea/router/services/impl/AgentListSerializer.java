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