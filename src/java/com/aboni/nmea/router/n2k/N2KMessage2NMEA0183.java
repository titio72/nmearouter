package com.aboni.nmea.router.n2k;

import net.sf.marineapi.nmea.sentence.Sentence;

public interface N2KMessage2NMEA0183 {

    Sentence[] getSentence(N2KMessage message);

}
