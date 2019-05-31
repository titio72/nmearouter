package com.aboni.nmea.router;

import com.aboni.nmea.router.agent.NMEAAgent;

public interface NMEASentenceListener {

	void onSentence(RouterMessage message, NMEAAgent src);

}
