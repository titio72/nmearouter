package com.aboni.nmea.router.agent;

import net.sf.marineapi.nmea.sentence.Sentence;

public interface NMEASentenceListener {

	void onSentence(Sentence s, NMEAAgent src);
	
}
