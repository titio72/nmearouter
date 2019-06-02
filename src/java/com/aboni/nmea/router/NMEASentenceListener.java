package com.aboni.nmea.router;

public interface NMEASentenceListener {
	void onSentence(RouterMessage message);
}
