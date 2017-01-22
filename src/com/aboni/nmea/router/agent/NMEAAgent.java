package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.NMEAAgentStatusListener;
import com.aboni.nmea.router.Startable;

public interface NMEAAgent extends Startable {
	
	String getName();
	
	boolean isBuiltIn();
	
	void setStatusListener(NMEAAgentStatusListener listener);
	void unsetStatusListener();
    
	NMEASource getSource();
	NMEATarget getTarget();

	public boolean isUserCanStartAndStop();
	
}
