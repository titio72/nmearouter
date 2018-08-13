package com.aboni.nmea.router.processors;

import net.sf.marineapi.nmea.sentence.Sentence;

public interface NMEAPostProcess {

	Sentence[] process(Sentence sentence, String src);
	
}
