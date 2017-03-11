package com.aboni.nmea.router.conf.db;

public class AgentStatusProvider {

	private AgentStatusProvider() {}
	
	private static AgentStatus instance;
	
	public static synchronized AgentStatus getAgentStatus() {
		if (instance==null) {
			instance = new AgentStatusImpl();
		}
		return instance;
	}
}
