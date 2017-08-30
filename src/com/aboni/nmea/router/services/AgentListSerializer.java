package com.aboni.nmea.router.services;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.conf.db.AgentStatusProvider;
import com.aboni.nmea.router.conf.db.AgentStatus.STATUS;

public class AgentListSerializer {
	public NMEARouter router;

	public AgentListSerializer(NMEARouter router) {
		this.router = router; 
	}
	
	public synchronized void dump(PrintWriter w, String message) throws IOException {
        w.println("{");
        w.println("\"message\":\""+ message + "\",");
        w.println("\"agents\":[");        Collection<String> agentKeys = router.getAgents();
        dumpServices(agentKeys, w);
        w.println("]}");
	}
    	
	boolean first = true;
	
	private void dumpService(NMEAAgent ag, PrintWriter w) throws IOException {
		if (!first)
            w.print(",");

		boolean auto = AgentStatusProvider.getAgentStatus().getStartMode(ag.getName())==STATUS.AUTO;
		String outFilter = AgentStatusProvider.getAgentStatus().getFilterOutData(ag.getName());
		
		first = false;
		w.println(
                "{\"agent\":\"" + ag.getName() + "\", " + 
                "\"description\":\"" + ag.getDescription() + "\", " + 
                "\"type\":\"" + ag.getClass().getSimpleName() + "\", " + 
                "\"started\":\"" + ag.isStarted() + "\", " + 
                "\"source\":\"" + (ag.getSource()!=null) + "\", " + 
                "\"target\":\"" + (ag.getTarget()!=null) + "\", " + 
                "\"startStop\":\"" + ag.isUserCanStartAndStop() + "\", " + 
                "\"builtin\":\"" + ag.isBuiltIn() + "\", " + 
                "\"auto\":\"" + auto + "\"," +
                "\"hasFilterOut\":\"" + (outFilter!=null) +"\"" +
                ((outFilter!=null)?(",\"filterOut\":" + outFilter):"") +
                "}");
	}
    
	private void dumpServices(Collection<String> agentKeys, PrintWriter w) throws IOException {
		first = true;
		for (Iterator<String> i = agentKeys.iterator(); i.hasNext(); ) {
		    String agentKey = i.next();
		    NMEAAgent ag = router.getAgent(agentKey);
	    	dumpService(ag, w);
		}
	}

	
}