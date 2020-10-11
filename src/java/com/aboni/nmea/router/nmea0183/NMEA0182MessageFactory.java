package com.aboni.nmea.router.nmea0183;

import com.aboni.nmea.router.message.Message;
import net.sf.marineapi.nmea.sentence.Sentence;

public interface NMEA0182MessageFactory {

    Message getMessage(Sentence sentence);

}
