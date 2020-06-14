package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.n2k.CANBOATDecoder;
import com.aboni.nmea.router.n2k.CANBOATDecoderImpl;
import com.aboni.nmea.router.n2k.CANBOATStream;
import com.aboni.utils.Log;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;

public class NMEAInputManager {

    private final CANBOATDecoder decoder;
    private final CANBOATStream n2kStream;
    private final Log logger;

    public NMEAInputManager(@NotNull Log logger) {
        decoder = new CANBOATDecoderImpl();
        n2kStream = new CANBOATStream();
        this.logger = logger;
    }

    public Sentence getSentence(String sSentence) {
        if (sSentence.startsWith("{\"timestamp\":\"")) {
            return handleN2K(sSentence);
        } else if (sSentence.charAt(0) == '$' || sSentence.charAt(0) == '!') {
            return handleN0183(sSentence);
        } else {
            logger.debug("Unknown sentence {" + sSentence + "}");
            return null;
        }
    }

    private Sentence handleN0183(String sSentence) {
        try {
            Sentence s = SentenceFactory.getInstance().createParser(sSentence);
            return s;
        } catch (Exception e) {
            logger.debug("Can't read NMEA sentence {" + sSentence + "} {" + e + "}");
            return null;
        }
    }

    private Sentence handleN2K(String sSentence) {
        try {
            JSONObject m = n2kStream.getMessage(sSentence);
            Sentence s = decoder.getSentence(m);
            return s;
        } catch (Exception e) {
            logger.debug("Can't read N2K sentence {" + sSentence + "} {" + e + "}");
            return null;
        }
    }
}
