package com.aboni.nmea.router.services;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.agent.impl.simulator.NMEASimulatorSource;
import org.json.JSONObject;

@SuppressWarnings("CatchMayIgnoreException")
public class SImulatorService implements WebService {

	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
        response.setContentType("text/plain;charset=utf-8");

        try {
        	if (NMEASimulatorSource.getSimulator()!=null) {
	        	String sSpeed = config.getParameter("speed");
	        	String sWSpeed = config.getParameter("wspeed");
	        	String sWDir = config.getParameter("wdir");
	        	String sHeading = config.getParameter("head");
	
	        	
	        	if (sSpeed!=null) {
	        		try {
	        			double speed = Double.parseDouble(sSpeed);
	        			NMEASimulatorSource.getSimulator().setSpeed(speed);
	        		} catch (Exception e) {}
	        	}
	        	if (sWSpeed!=null) {
	        		try {
	        			double ws = Double.parseDouble(sWSpeed);
	        			NMEASimulatorSource.getSimulator().setwSpeed(ws);
	        		} catch (Exception e) {}
	        	}
	        	if (sWDir!=null) {
	        		try {
	        			double a = Double.parseDouble(sWDir);
	        			NMEASimulatorSource.getSimulator().setwDirection(Utils.normalizeDegrees0_360(a));
	        		} catch (Exception e) {}
	        	}
	        	if (sHeading!=null) {
	        		try {
	        			double h = Double.parseDouble(sHeading);
	        			NMEASimulatorSource.getSimulator().setHeading(Utils.normalizeDegrees0_360(h));
	        		} catch (Exception e) {}
	        	}
        	}
        	response.setContentType("application/json");
        	JSONObject res = new JSONObject();
        	res.put("speed", NMEASimulatorSource.getSimulator().getSpeed());
        	res.put("wspeed", NMEASimulatorSource.getSimulator().getwSpeed());
        	res.put("wdir", NMEASimulatorSource.getSimulator().getwDirection());
        	res.put("heading", NMEASimulatorSource.getSimulator().getHeading());
            response.getWriter().println(res.toString());
            response.ok();
        } catch (Exception e) {
            response.setContentType("text/html;charset=utf-8");
            try {
				response.error(e.getMessage());
            	e.printStackTrace(response.getWriter());
            } catch (Exception ee) {}
        }

	}

}
