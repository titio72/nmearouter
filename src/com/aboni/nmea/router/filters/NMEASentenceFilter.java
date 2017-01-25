package com.aboni.nmea.router.filters;

import net.sf.marineapi.nmea.sentence.Sentence;

public interface NMEASentenceFilter {
	
	boolean match(Sentence s, String src);
	
}
