package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.n2k.CANBOATDecoder;
import com.aboni.nmea.router.n2k.CANBOATStream;
import com.aboni.nmea.router.n2k.PGNMessage;
import com.aboni.nmea.router.n2k.impl.CANBOATDecoderImpl;
import com.aboni.nmea.router.n2k.impl.CANBOATStreamImpl;
import com.aboni.utils.Log;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.validation.constraints.NotNull;

public class NMEAInputManager {

    private final CANBOATDecoder decoder;
    private final CANBOATStream n2kStream;
    private final Log logger;

    public NMEAInputManager(@NotNull Log logger) {
        decoder = new CANBOATDecoderImpl();
        n2kStream = new CANBOATStreamImpl(logger);
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
            return SentenceFactory.getInstance().createParser(sSentence);
        } catch (Exception e) {
            logger.debug("Can't read NMEA sentence {" + sSentence + "} {" + e + "}");
        }
        return null;
    }

    private Sentence handleN2K(String sSentence) {
        try {
            PGNMessage msg = n2kStream.getMessage(sSentence);
            if (msg != null && msg.getFields() != null) {
                return decoder.getSentence(msg.getFields());
            }
        } catch (Exception e) {
            logger.debug("Can't read N2K sentence {" + sSentence + "} {" + e + "}");
        }
        return null;
    }
}
