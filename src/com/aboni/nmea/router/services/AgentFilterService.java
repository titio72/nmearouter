package com.aboni.nmea.router.services;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.conf.db.AgentStatusProvider;
import com.aboni.nmea.router.conf.db.AgentStatus.STATUS;
import com.aboni.nmea.router.filters.FilterSetBuilder;
import com.aboni.nmea.router.filters.NMEABasicSentenceFilter;
import com.aboni.nmea.router.filters.NMEAFilterSet;
import com.aboni.nmea.router.filters.NMEAFilterSet.TYPE;

public class AgentFilterService implements WebService {

	private NMEARouter router;
	
	public AgentFilterService(NMEARouter router) {
		this.router = router;
	}
	
    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {
        response.setContentType("application/json");
        try {
        	String agentname = config.getParameter("agent"); 
        	String[] sentences = config.getParameter("sentences").split(",");
            String type = config.getParameter("type");
            
            NMEAFilterSet fs = new NMEAFilterSet("whitelist".equals(type)?TYPE.WHITELIST:TYPE.BLACKLIST);
            for (String sentence: sentences) {
            	NMEABasicSentenceFilter f = new NMEABasicSentenceFilter(sentence);
            	fs.addFilter(f);
            }
            
            NMEAAgent a = router.getAgent(agentname);
            if (a!=null) {
            	a.getTarget().setFilter(fs);
            	String sfs = new FilterSetBuilder().exportFilter(fs);
            	AgentStatusProvider.getAgentStatus().setFilterOutData(agentname, sfs);
            }
            
            response.getWriter().println("{");
            response.getWriter().println("\"message\":\"\",");
            response.getWriter().println("\"agents\":[");

            
            Collection<String> agentKeys = router.getAgents();
            
            ServiceDumper d = new ServiceDumper(response);
            dumpServices(d, agentKeys);
            
            response.getWriter().println("]}");
            response.ok();
            
        } catch (Exception e) {
            response.setContentType("text/html;charset=utf-8");
            try { e.printStackTrace(response.getWriter()); } catch (Exception ee) {}
            response.error(e.getMessage());
        }
        
    }

    private class ServiceDumper {
    	
    	boolean first = true;
    	ServiceOutput response;
    	
    	ServiceDumper(ServiceOutput response) {
    		this.response = response;
    	}
    	
    	void dumpServices(NMEAAgent ag) throws IOException {
    		if (!first)
                response.getWriter().print(",");

    		boolean auto = AgentStatusProvider.getAgentStatus().getStartMode(ag.getName())==STATUS.AUTO;
    		String outFilter = AgentStatusProvider.getAgentStatus().getFilterOutData(ag.getName());
    		
    		first = false;
    		response.getWriter().print(
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
    	
    }
    
	private void dumpServices(ServiceDumper r, Collection<String> agentKeys) throws IOException {
		for (Iterator<String> i = agentKeys.iterator(); i.hasNext(); ) {
		    String agentKey = i.next();
		    NMEAAgent ag = router.getAgent(agentKey);
	    	r.dumpServices(ag);
		}
	}

    
}
