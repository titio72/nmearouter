package com.aboni.nmea.router.filters;

import net.sf.marineapi.nmea.sentence.Sentence;

public interface NMEAFilter {

    boolean match(Sentence s, String src);

}
