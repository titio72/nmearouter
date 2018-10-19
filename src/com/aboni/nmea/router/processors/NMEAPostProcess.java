package com.aboni.nmea.router.processors;

import com.aboni.utils.Pair;

import net.sf.marineapi.nmea.sentence.Sentence;

public interface NMEAPostProcess {

	/**
	 * Post-process a sentence before it is sent to the router. Typically used to manipulate/enrich sentences
	 * or to elaborate additional sentences derived from the input sentence.
	 * @param sentence The input sentence to be processed
	 * @param src The source where the sentence comes from
	 * @return a Pair<> where the first value is a boolean that indicates whether the 
	 * sentence must be skipped completely (false=skip). In case the first value is true then
	 * the second member contains an array of *additional* sentences to be sent out.
	 */
	Pair<Boolean, Sentence[]> process(Sentence sentence, String src);
	
	/**
	 * supposed to be called every 1 seconds
	 */
	void onTimer();
	
}
