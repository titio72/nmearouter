package com.aboni.nmea.router.services;

import org.json.JSONObject;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.agent.NMEASimulatorSource;

public class SImulatorService implements WebService {

	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
        response.setContentType("text/plain;charset=utf-8");

        try {
        	if (NMEASimulatorSource.SIMULATOR!=null) {
	        	String sSpeed = config.getParameter("speed");
	        	String sWSpeed = config.getParameter("wspeed");
	        	String sWDir = config.getParameter("wdir");
	        	String sHeading = config.getParameter("head");
	
	        	
	        	if (sSpeed!=null) {
	        		try {
	        			double speed = Double.parseDouble(sSpeed);
	        			NMEASimulatorSource.SIMULATOR.setSpeed(speed);
	        		} catch (Exception e) {}
	        	}
	        	if (sWSpeed!=null) {
	        		try {
	        			double ws = Double.parseDouble(sWSpeed);
	        			NMEASimulatorSource.SIMULATOR.setwSpeed(ws);
	        		} catch (Exception e) {}
	        	}
	        	if (sWDir!=null) {
	        		try {
	        			double a = Double.parseDouble(sWDir);
	        			NMEASimulatorSource.SIMULATOR.setwDirection(Utils.normalizeDegrees0_360(a));
	        		} catch (Exception e) {}
	        	}
	        	if (sHeading!=null) {
	        		try {
	        			double h = Double.parseDouble(sHeading);
	        			NMEASimulatorSource.SIMULATOR.setHeading(Utils.normalizeDegrees0_360(h));
	        		} catch (Exception e) {}
	        	}
        	}
        	response.setContentType("application/json");
        	JSONObject res = new JSONObject();
        	res.put("speed", NMEASimulatorSource.SIMULATOR.getSpeed());
        	res.put("wspeed", NMEASimulatorSource.SIMULATOR.getwSpeed());
        	res.put("wdir", NMEASimulatorSource.SIMULATOR.getwDirection());
        	res.put("heading", NMEASimulatorSource.SIMULATOR.getHeading());
            response.getWriter().println(res.toString());
            response.ok();
        } catch (Exception e) {
            response.setContentType("text/html;charset=utf-8");
            try { e.printStackTrace(response.getWriter()); } catch (Exception ee) {}
            response.error(e.getMessage());
        }

	}

}
