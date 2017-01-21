package com.aboni.nmea.router;

import com.aboni.nmea.router.agent.NMEAAgent;

public interface NMEAAgentStatusListener {
	
	void onStatusChange(NMEAAgent t);

}
