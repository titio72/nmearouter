package com.aboni.nmea.router.conf.db;

public interface AgentStatus {

	public enum STATUS {
		AUTO,
		MANUAL,
		UNKNOWN
	}
	
	STATUS getStartMode(String agent);
	void setStartMode(String agent, STATUS status);
	
	String getAgentData(String agent);
	void setAgentData(String agent, String data);
}
