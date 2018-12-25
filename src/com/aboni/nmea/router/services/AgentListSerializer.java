package com.aboni.nmea.router.services;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.conf.db.AgentStatus.STATUS;
import com.aboni.nmea.router.conf.db.AgentStatusProvider;

import java.io.PrintWriter;
import java.util.Collection;

public class AgentListSerializer {

	private final NMEARouter router;

	public AgentListSerializer(NMEARouter router) {
		this.router = router; 
	}
	
	public synchronized void dump(PrintWriter w, String message) {
        w.println("{");
        w.println("\"message\":\""+ message + "\",");
        w.println("\"agents\":[");        
        Collection<String> agentKeys = router.getAgents();
        dumpServices(agentKeys, w);
        w.println("]}");
	}
    	
	private boolean first = true;
	
	private void dumpService(NMEAAgent ag, PrintWriter w) {
		if (!first)
            w.print(",");

		boolean auto = AgentStatusProvider.getAgentStatus().getStartMode(ag.getName())==STATUS.AUTO;
		String outFilter = AgentStatusProvider.getAgentStatus().getFilterOutData(ag.getName());
		String inFilter = AgentStatusProvider.getAgentStatus().getFilterInData(ag.getName());
		outFilter = (outFilter!=null && outFilter.isEmpty())?null:outFilter;
		inFilter = (inFilter!=null && inFilter.isEmpty())?null:inFilter;
		
		first = false;
		w.println(
                "{\"agent\":\"" + ag.getName() + "\", " + 
                "\"description\":\"" + ag.getDescription() + "\", " + 
                "\"type\":\"" + ag.getType() + "\", " + 
                "\"started\":\"" + ag.isStarted() + "\", " + 
                "\"source\":\"" + (ag.getSource()!=null) + "\", " + 
                "\"target\":\"" + (ag.getTarget()!=null) + "\", " + 
                "\"startStop\":\"" + ag.isUserCanStartAndStop() + "\", " + 
                "\"builtin\":\"" + ag.isBuiltIn() + "\", " + 
                "\"auto\":\"" + auto + "\"," +
                "\"hasFilterIn\":\"" + (inFilter!=null) +"\"," +
                ((inFilter!=null)?("\"filterIn\":" + inFilter + ","):"") +
                "\"hasFilterOut\":\"" + (outFilter!=null) +"\"" +
                ((outFilter!=null)?(",\"filterOut\":" + outFilter):"") +
                "}");
	}
    
	private void dumpServices(Collection<String> agentKeys, PrintWriter w) {
		first = true;
		for (String agentKey : agentKeys) {
			NMEAAgent ag = router.getAgent(agentKey);
			dumpService(ag, w);
		}
	}

	
}