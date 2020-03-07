package com.aboni.nmea.router.services.impl;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.conf.db.AgentStatus.STATUS;
import com.aboni.nmea.router.conf.db.AgentStatusProvider;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.PrintWriter;

public class AgentListSerializer {

    private final NMEARouter router;

    public AgentListSerializer(NMEARouter router) {
        this.router = router;
    }

    public JSONObject getJSON(String message) {
        JSONObject res = new JSONObject();
        res.put("message", message);
        JSONArray servicesJSON = new JSONArray();
        res.put("agents", servicesJSON);
        for (String agentName : router.getAgents()) {
            NMEAAgent ag = router.getAgent(agentName);
            boolean auto = AgentStatusProvider.getAgentStatus().getStartMode(ag.getName()) == STATUS.AUTO;
            String outFilter = AgentStatusProvider.getAgentStatus().getFilterOutData(ag.getName());
            String inFilter = AgentStatusProvider.getAgentStatus().getFilterInData(ag.getName());
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

    public synchronized void dump(PrintWriter w, String message) {
        JSONObject res = getJSON(message);
        w.print(res.toString(2));
    }
}