package com.aboni.nmea.router;

import com.aboni.nmea.router.agent.NMEAAgent;

import net.sf.marineapi.nmea.sentence.Sentence;

public interface NMEASentenceListener {

	void onSentence(Sentence s, NMEAAgent src);
	
}
