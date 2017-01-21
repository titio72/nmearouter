package com.aboni.nmea.router.filters;

import net.sf.marineapi.nmea.sentence.Sentence;

public interface NMEASentenceFilter {

	enum FILTERACTION {
		ACCEPT,
		REJECT,
		NONE
	}
	
	FILTERACTION accept(Sentence s, String src);
	
}
