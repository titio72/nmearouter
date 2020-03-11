package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.sentences.NMEA2JSONb;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class NMEASocketServerJSON extends NMEASocketServer {

    private final NMEA2JSONb js;

    @Override
    public String getType() {
        return "TCP Json Server";
    }

    @Inject
    public NMEASocketServerJSON(@NotNull NMEACache cache) {
        super(cache);
        js = new NMEA2JSONb();
    }

    @Override
    protected String getOutSentence(Sentence s) {
        return js.convert(s).toString();
    }
}
