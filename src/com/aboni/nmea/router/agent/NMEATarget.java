package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.NMEAFilterable;

import net.sf.marineapi.nmea.sentence.Sentence;

public interface NMEATarget extends NMEAFilterable {

    void pushSentence(Sentence e, NMEAAgent src);
}