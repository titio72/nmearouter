package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.NMEAFilterable;

import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

public interface NMEATarget extends NMEAFilterable {

    void pushSentence(Sentence e, String src);
}