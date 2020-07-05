/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.n2k.*;
import com.aboni.nmea.router.n2k.impl.CANBOATDecoderImpl;
import com.aboni.nmea.router.n2k.impl.CANBOATStreamImpl;
import com.aboni.utils.Log;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.validation.constraints.NotNull;

public class NMEAInputManager {

    private PGNs pgnDefs;
    private final CANBOATDecoder decoder;
    private final CANBOATStream n2kStream;
    private final Log logger;

    public NMEAInputManager(@NotNull Log logger) {
        decoder = new CANBOATDecoderImpl();
        n2kStream = new CANBOATStreamImpl(logger);
        try {
            pgnDefs = new PGNs(Constants.CONF_DIR + "/pgns.json", logger);
        } catch (PGNDefParseException e) {
            logger.error("Cannot load pgns definitions", e);
            pgnDefs = null;
        }
        this.logger = logger;
    }

    public Sentence[] getSentence(String sSentence) {
        if (sSentence.startsWith("{\"timestamp\":\"")) {
            return handleN2KCanboat(sSentence);
        } else if (sSentence.charAt(0) == '$' || sSentence.charAt(0) == '!') {
            return new Sentence[]{handleN0183(sSentence)};
        } else if (pgnDefs != null && sSentence.charAt(0) >= '1' && sSentence.charAt(0) <= '2') {
            return handleN2K(sSentence);
        } else {
            logger.debug("Unknown sentence {" + sSentence + "}");
            return new Sentence[]{};
        }
    }

    private Sentence[] handleN2K(String sSentence) {
        try {
            PGNParser p = new PGNParser(pgnDefs, sSentence.trim());
            CANBOATPGNMessage msg = n2kStream.getMessage(p.getCanBoatJson());
            if (msg != null && msg.getFields() != null) {
                return decoder.getSentence(msg.getPgn(), msg.getFields());
            }
        } catch (PGNParser.PGNDataParseException e) {
            if (!e.isUnsupportedPGN()) {
                logger.debug("Cannot parse n2k sentence {" + sSentence + "} {" + e.getMessage() + "}");
            }
        } catch (Exception e) {
            logger.debug("Cannot parse n2k sentence {" + sSentence + "} {" + e.getMessage() + "}");
        }
        return new Sentence[] {};
    }

    private Sentence handleN0183(String sSentence) {
        try {
            return SentenceFactory.getInstance().createParser(sSentence);
        } catch (Exception e) {
            logger.debug("Can't read NMEA sentence {" + sSentence + "} {" + e + "}");
        }
        return null;
    }

    private Sentence[] handleN2KCanboat(String sSentence) {
        try {
            CANBOATPGNMessage msg = n2kStream.getMessage(sSentence);
            if (msg != null && msg.getFields() != null) {
                return decoder.getSentence(msg.getPgn(), msg.getFields());
            }
        } catch (Exception e) {
            logger.debug("Can't read N2K sentence {" + sSentence + "} {" + e + "}");
        }
        return new Sentence[]{};
    }
}
