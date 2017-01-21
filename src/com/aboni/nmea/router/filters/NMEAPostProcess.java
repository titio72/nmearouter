package com.aboni.nmea.router.filters;

import net.sf.marineapi.nmea.sentence.Sentence;

public interface NMEAPostProcess {

	Sentence[] process(Sentence sentence, String src);
	
}
