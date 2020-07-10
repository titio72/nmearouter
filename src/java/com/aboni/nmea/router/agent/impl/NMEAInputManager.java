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
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessage2NMEA0183;
import com.aboni.nmea.router.n2k.N2KStream;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.canboat.*;
import com.aboni.nmea.router.n2k.canboat.impl.CANBOATDecoderImpl;
import com.aboni.nmea.router.n2k.canboat.impl.CANBOATStreamImpl;
import com.aboni.nmea.router.n2k.impl.N2KMessage2NMEA0183Impl;
import com.aboni.nmea.router.n2k.impl.N2KStreamImpl;
import com.aboni.utils.Log;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.validation.constraints.NotNull;

public class NMEAInputManager {

    private interface StringInputHandler {
        Output getSentences(String pgn);
    }

    private static class N2KHandlerStd implements StringInputHandler {
        private PGNs pgnDefinitions;
        private final CANBOATDecoder decoder;
        private final CANBOATStream canboatStream;
        private final Log logger;

        N2KHandlerStd(Log logger) {
            this.logger = logger;
            try {
                pgnDefinitions = new PGNs(Constants.CONF_DIR + "/pgns.json", logger);
            } catch (PGNDefParseException e) {
                logger.error("Cannot load pgns definitions", e);
                pgnDefinitions = null;
            }
            decoder = new CANBOATDecoderImpl();
            canboatStream = new CANBOATStreamImpl(logger);
        }

        @Override
        public Output getSentences(String pgn) {
            if (pgnDefinitions == null) return getEmpty(pgn);
            try {
                PGNParser p = new PGNParser(pgnDefinitions, pgn.trim());
                CANBOATPGNMessage msg = canboatStream.getMessage(p.getCanBoatJson());
                if (msg != null && msg.getFields() != null) {
                    return new Output(pgn, decoder.getSentence(msg.getPgn(), msg.getFields()), null);
                }
            } catch (PGNDataParseException e) {
                if (!e.isUnsupported()) {
                    logger.debug(getErrorString(pgn, e));
                }
            } catch (Exception e) {
                logger.warning(getErrorString(pgn, e));
            }
            return getEmpty(pgn);
        }
    }

    private static class N2KHandlerExp implements StringInputHandler {
        private final N2KMessage2NMEA0183 decoder;
        private final N2KStream stream;
        private final Log logger;

        N2KHandlerExp(Log logger) {
            this.logger = logger;
            decoder = new N2KMessage2NMEA0183Impl();
            stream = new N2KStreamImpl(logger);
        }

        @Override
        public Output getSentences(String pgn) {
            try {
                N2KMessage msg = stream.getMessage(pgn);
                if (msg != null) {
                    return new Output(pgn, decoder.getSentence(msg), msg);
                }
            } catch (Exception e) {
                logger.warning(getErrorString(pgn, e));
            }
            return getEmpty(pgn);
        }
    }

    private static class JSONHandler implements StringInputHandler {
        private final CANBOATDecoder decoder;
        private final CANBOATStream canboatStream;
        private final Log logger;

        JSONHandler(Log logger) {
            this.logger = logger;
            decoder = new CANBOATDecoderImpl();
            canboatStream = new CANBOATStreamImpl(logger);
        }

        @Override
        public Output getSentences(String pgn) {
            try {
                CANBOATPGNMessage msg = canboatStream.getMessage(pgn);
                if (msg != null && msg.getFields() != null) {
                    return new Output(pgn, decoder.getSentence(msg.getPgn(), msg.getFields()), null);
                }
            } catch (Exception e) {
                logger.debug("Can't read N2K sentence {" + pgn + "} {" + e + "}");
            }
            return getEmpty(pgn);
        }
    }

    private static class NMEA0183Handler implements StringInputHandler {

        private final Log logger;

        NMEA0183Handler(Log logger) {
            this.logger = logger;
        }

        @Override
        public Output getSentences(String sSentence) {
            try {
                return new Output(sSentence, new Sentence[]{SentenceFactory.getInstance().createParser(sSentence)}, null);
            } catch (Exception e) {
                logger.debug("Can't read NMEA sentence {" + sSentence + "} {" + e + "}");
            }
            return getEmpty(sSentence);
        }
    }

    private final Log logger;
    private static final boolean EXPERIMENTAL = true;
    private final StringInputHandler n2kHandler;
    private final StringInputHandler jsonHandler;
    private final StringInputHandler nmeaHandler;

    public NMEAInputManager(@NotNull Log logger) {
        n2kHandler = EXPERIMENTAL ? new N2KHandlerExp(logger) : new N2KHandlerStd(logger);
        jsonHandler = new JSONHandler(logger);
        nmeaHandler = new NMEA0183Handler(logger);
        this.logger = logger;
    }

    public static class Output {
        Output(String s, Sentence[] ss, N2KMessage m) {
            original = s;
            nmeaSentences = ss;
            n2KMessage = m;
        }

        String original;
        N2KMessage n2KMessage;
        Sentence[] nmeaSentences;

        public boolean isEmpyy() {
            return n2KMessage == null && (nmeaSentences == null || nmeaSentences.length == 0);
        }
    }

    private static Output getEmpty(String orig) {
        return new Output(orig, new Sentence[0], null);
    }

    public Output getSentence(String sSentence) {
        if (sSentence.startsWith("{\"timestamp\":\"")) {
            return jsonHandler.getSentences(sSentence);
        } else if (sSentence.charAt(0) == '$' || sSentence.charAt(0) == '!') {
            return nmeaHandler.getSentences(sSentence);
        } else if (sSentence.charAt(0) >= '1' && sSentence.charAt(0) <= '2') {
            return n2kHandler.getSentences(sSentence);
        } else {
            logger.debug("Cannot find a suitable handler for {" + sSentence + "}");
            return getEmpty(sSentence);
        }
    }

    private static String getErrorString(String s, Exception e) {
        return String.format("Cannot parse n2k sentence {%s} {%s}", s, e.getMessage());
    }
}
