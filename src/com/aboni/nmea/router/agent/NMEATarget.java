package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.filters.NMEASentenceFilterSet;

import net.sf.marineapi.nmea.sentence.Sentence;

public interface NMEATarget {

    NMEASentenceFilterSet getTargetFilter();

    void pushSentence(Sentence e, NMEAAgent src);

}