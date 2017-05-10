package com.aboni.nmea.router;

import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEASentenceListener;

import net.sf.marineapi.nmea.sentence.Sentence;

public interface NMEAStream {

	void addSentenceListener(NMEASentenceListener listener);
	void dropSentenceListener(NMEASentenceListener listener);
	void pushSentence(Sentence s, NMEAAgent src);

	void subscribe(Object b);
	void unsubscribe(Object b);
	
}
