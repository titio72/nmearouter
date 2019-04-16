package com.aboni.nmea.router.services;

import com.aboni.nmea.router.AutoPilotDriver;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.utils.ServerLog;

public class AutoPilotService  implements WebService {

	private final AutoPilotDriver auto;
	
	public AutoPilotService(NMEARouter router) {
		this.auto = (AutoPilotDriver)router.getAgent("SmartPilot");
	}

	public static final String CMD_STARBOARD_1 = "S1";
	public static final String CMD_PORT_1 = "P1";
	public static final String CMD_STARBOARD_10 = "S10";
	public static final String CMD_PORT_10 = "P10";
	public static final String CMD_AUTO  = "Auto";
	public static final String CMD_STDBY = "Standby";
	public static final String CMD_WVANE = "Wind";
	
	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
		try {
			String command = config.getParameter("command");
			switch (command) {
			case CMD_PORT_10:
				auto.port10();
				break;
			case CMD_PORT_1:
				auto.port1();
				break;
			case CMD_STARBOARD_10:
				auto.starboard10();
				break;
			case CMD_STARBOARD_1:
				auto.starboard1();
				break;
			case CMD_AUTO:
				auto.enable();
				break;
			case CMD_STDBY:
				auto.standBy();
				break;
			case CMD_WVANE:
				auto.windVane();
				break;
			}
	        response.setContentType("application/json");
	        response.getWriter().println("{}");
	        response.ok();
		} catch (Exception e) {
	        response.setContentType("application/json");
	        try {
	        	response.getWriter().println("{\"error\", \"" + e.getMessage() + "\"}");
	        } catch (Exception ee) {
				ServerLog.getLogger().Error("Error sending response to web client", ee);
			}
	        response.ok();
		}
	}
}
