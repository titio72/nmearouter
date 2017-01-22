package com.aboni.nmea.router.services;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import com.aboni.nmea.router.NMEARouterProvider;
import com.aboni.nmea.router.agent.NMEAAgent;

public class AgentStatusServiceJSON implements WebService {

    public AgentStatusServiceJSON() {
    }

    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {
        response.setContentType("application/json");

        try {
            String msg = doActivate(config);
            
            response.getWriter().println("{");
            response.getWriter().println("\"message\":\"" + msg + "\",");
            response.getWriter().println("\"agents\":[");

            
            Collection<String> agentKeys = NMEARouterProvider.getRouter().getAgents();
            
            ServiceDumper d = new ServiceDumper(response);
            dumpServices(d, agentKeys, true);
            dumpServices(d, agentKeys, false);
            
            response.getWriter().println("]}");
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
    		
    		first = false;
    		response.getWriter().print(
                "{\"agent\":\"" + ag.getName() + "\", " + 
                        "\"type\":\"" + ag.getClass().getSimpleName() + "\", " + 
                        "\"started\":\"" + ag.isStarted() + "\", " + 
                        "\"source\":\"" + (ag.getSource()!=null) + "\", " + 
                        "\"target\":\"" + (ag.getTarget()!=null) + "\", " + 
                        "\"startStop\":\"" + ag.isUserCanStartAndStop() + "\", " + 
                        "\"builtin\":\"" + ag.isBuiltIn() + "\"}");
    	}
    	
    }
    
	private void dumpServices(ServiceDumper r, Collection<String> agentKeys, boolean builtIn) throws IOException {
		for (Iterator<String> i = agentKeys.iterator(); i.hasNext(); ) {
		    String agentKey = i.next();
		    NMEAAgent ag = NMEARouterProvider.getRouter().getAgent(agentKey);
		    if (ag!=null && ag.isBuiltIn()==builtIn) {
		    	r.dumpServices(ag);
		    }
		}
	}

    private String doActivate(ServiceConfig config) {
		String msg = "";
		String agent = config.getParameter("agent");
		if (agent!=null) {
			NMEAAgent a = NMEARouterProvider.getRouter().getAgent(agent);
			if (a!=null) {
				if (a.isBuiltIn()) {
					msg = "Cannot change activation status for built in agents";
				} else {
		        	String activate = config.getParameter("active");
		        	if (activate.toUpperCase().equals("YES") || activate.equals("1")) {
		        		if (a.isStarted()) {
		        			msg = "Agent '" + agent + "' alread started";
		        		} else {
		        			a.start();
		        			msg = "Agent '" + agent + "' started";
		        		}
		        	} else if (activate.toUpperCase().equals("NO") || activate.equals("0")) {
		        		if (a.isStarted()) {
		        			a.stop();
		        			msg = "Agent '" + agent + "' stopped";
		        		} else {
		        			msg = "Agent '" + agent + "' not started";
		        		}
		        	} else {
		        		msg = "Unknown status '" + activate + "'"; 
		        	}
				}
			} else {
				msg = "Unknown agent '" + agent + "'";
			}
		}
		return msg;
	}
    

}
