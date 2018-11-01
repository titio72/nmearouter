package com.aboni.nmea.router.agent;

import org.json.JSONObject;

public interface NMEAAgentStatusListener {
	
	void onStatusChange(NMEAAgent t);
	void onData(JSONObject s, NMEAAgent src);

}
