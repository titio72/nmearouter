package com.aboni.nmea.router.services;

import java.util.Collection;
import java.util.Iterator;

import com.aboni.nmea.router.NMEARouterProvider;
import com.aboni.nmea.router.agent.NMEAAgent;

public class AgentStatusService implements WebService {

    public AgentStatusService() {
    }
    
    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {
        response.setContentType("text/xml;charset=utf-8");

        try {
            response.getWriter().println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

            String msg = doActivate(config);
            
            response.getWriter().println("<RouterAgentStatus>");
            response.getWriter().println("<Message>" + msg + "</Message>");
            response.getWriter().println("<Agents>");

            
            Collection<String> agentKeys = NMEARouterProvider.getRouter().getAgents();
            for (Iterator<String> i = agentKeys.iterator(); i.hasNext(); ) {
                String agentKey = i.next();
                NMEAAgent ag = NMEARouterProvider.getRouter().getAgent(agentKey);
                if (ag!=null) {
                    response.getWriter().println(
                            "<Agent  name=\"" + ag.getName() + "\" " + 
                                    "started=\"" + ag.isStarted() + "\" " + 
                                    "source=\"" + (ag.getSource()!=null) + "\" " + 
                                    "target=\"" + (ag.getTarget()!=null) + "\" " + 
                                    "startStop=\"" + ag.isUserCanStartAndStop() + "\" " + 
                                    "builtin=\"" + ag.isBuiltIn() + "\"/>");
                }
            }
            
            response.getWriter().println("</Agents>");
            response.getWriter().println("</RouterAgentStatus>");
        } catch (Exception e) {
            response.setContentType("text/html;charset=utf-8");
            try { e.printStackTrace(response.getWriter()); } catch (Exception ee) {}
            response.error(e.getMessage());
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
