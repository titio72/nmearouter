package com.aboni.nmea.router.agent;

public interface AgentStatusManager {

    enum STATUS {
        AUTO,
        MANUAL,
        UNKNOWN
    }

    STATUS getStartMode(String agent);

    void setStartMode(String agent, STATUS status);
	
	String getFilterOutData(String agent);
	void setFilterOutData(String agent, String data);

	String getFilterInData(String agent);
	void setFilterInData(String agent, String agData);

}
