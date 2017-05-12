package com.aboni.nmea.router.services;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import com.aboni.nmea.router.NMEARouterProvider;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.conf.db.AgentStatus;
import com.aboni.nmea.router.conf.db.AgentStatus.STATUS;
import com.aboni.nmea.router.conf.db.AgentStatusProvider;

public class AgentStatusServiceJSON implements WebService {

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
            dumpServices(d, agentKeys);
            
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

    		boolean auto = AgentStatusProvider.getAgentStatus().getStartMode(ag.getName())==STATUS.AUTO;
    		
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
                    "\"auto\":\"" + auto + "\"" + 
                    "}");
    	}
    	
    }
    
	private void dumpServices(ServiceDumper r, Collection<String> agentKeys) throws IOException {
		for (Iterator<String> i = agentKeys.iterator(); i.hasNext(); ) {
		    String agentKey = i.next();
		    NMEAAgent ag = NMEARouterProvider.getRouter().getAgent(agentKey);
	    	r.dumpServices(ag);
		}
	}

    private String doActivate(ServiceConfig config) {
		String msg = "";
		String agent = config.getParameter("agent");
		String auto = config.getParameter("auto");
		String active = config.getParameter("active");
		if (agent!=null) {
			NMEAAgent a = NMEARouterProvider.getRouter().getAgent(agent);
			if (a!=null) {
				if (active!=null) {
					msg = startStopService(a, active);
				} 
				
				if (auto!=null) {
					AgentStatus as = AgentStatusProvider.getAgentStatus();
					as.setStartMode(agent, "1".equals(auto)?STATUS.AUTO:STATUS.MANUAL);
				}
			} else {
				msg = "Unknown agent '" + agent + "'";
			}
		}
		return msg;
	}

	private String startStopService(NMEAAgent a, String activate) {
		String msg;
		if (a.isUserCanStartAndStop()) {
			if (activate.toUpperCase().equals("YES") || activate.equals("1")) {
				if (a.isStarted()) {
					msg = "Agent '" + a.getName() + "' alread started";
				} else {
					a.start();
					msg = "Agent '" + a.getName() + "' started";
				}
			} else if (activate.toUpperCase().equals("NO") || activate.equals("0")) {
				if (a.isStarted()) {
					a.stop();
					msg = "Agent '" + a.getName() + "' stopped";
				} else {
					msg = "Agent '" + a.getName() + "' not started";
				}
			} else {
				msg = "Unknown status '" + activate + "'"; 
			}
		} else {
			msg = "This agent does not support starting/stopping";
		}
		return msg;
	}
    

}
