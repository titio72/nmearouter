package com.aboni.nmea.router.n2k;

import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

public interface CANBOATDecoder {
    Sentence getSentence(JSONObject canBoatSentence);
}
