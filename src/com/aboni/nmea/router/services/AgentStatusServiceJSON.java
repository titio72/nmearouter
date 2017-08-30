package com.aboni.nmea.router.services;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.conf.db.AgentStatus;
import com.aboni.nmea.router.conf.db.AgentStatus.STATUS;
import com.aboni.nmea.router.conf.db.AgentStatusProvider;

public class AgentStatusServiceJSON implements WebService {

	private NMEARouter router;
	
	public AgentStatusServiceJSON(NMEARouter router) {
		this.router = router;
	}
	
    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {
        response.setContentType("application/json");

        try {
            String msg = doActivate(config);
            new AgentListSerializer(router).dump(response.getWriter(), msg);
            response.ok();
        } catch (Exception e) {
            try { e.printStackTrace(response.getWriter()); } catch (Exception ee) {}
            response.ok();
        }
        
    }

    private String doActivate(ServiceConfig config) {
		String msg = "";
		String agent = config.getParameter("agent");
		String auto = config.getParameter("auto");
		String active = config.getParameter("active");
		if (agent!=null) {
			NMEAAgent a = router.getAgent(agent);
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
