package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.Filterable;

import net.sf.marineapi.nmea.sentence.Sentence;

public interface NMEATarget {

    Filterable getTargetFilter();

    void pushSentence(Sentence e, NMEAAgent src);

}