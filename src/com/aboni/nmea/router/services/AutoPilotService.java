package com.aboni.nmea.router.services;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.autopilot.AutoPilot;

public class AutoPilotService  implements WebService {

	private AutoPilot auto;
	
	public AutoPilotService(NMEARouter router) {
	}

	public static final String STARBOARD = "S";
	public static final String PORT = "P";

	public static final String CMD_ANGLE  = "Angle";
	public static final String CMD_AUTO  = "Auto";
	public static final String CMD_STDBY = "Standby";
	public static final String CMD_WVANE = "Wind";
	
	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
		try {
			String command = config.getParameter("command");
			switch (command) {
			case CMD_ANGLE:
				changeAngleBy(config.getParameter("angle"), config.getParameter("side"));
			break;
			case CMD_AUTO:
				setAuto(true);
			break;
			case CMD_STDBY:
				setAuto(false);
			break;
			case CMD_WVANE:
				setWindVane();
			break;
			}
			
	        response.setContentType("application/json");
	        response.getWriter().println("{}");
	        response.ok();
		} catch (Exception e) {
	        response.setContentType("application/json");
	        try { response.getWriter().println("{\"error\", \"" + e.getMessage() + "\"}"); } catch (Exception ee) {}
	        response.ok();
		}
	}

	private void setAuto(boolean b) {
		auto.setAuto(b);
	}

	private void setWindVane() {
		auto.setWindVane();
	}

	private void changeAngleBy(String a, String s) {
		auto.changeAngleBy(Integer.parseInt(a), s);
	}
}
