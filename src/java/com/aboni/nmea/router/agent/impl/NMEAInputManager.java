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

import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KStream;
import com.aboni.nmea.router.nmea0183.NMEA0183Message;
import com.aboni.utils.Log;
import com.aboni.utils.ThingsFactory;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.validation.constraints.NotNull;

/**
 * Extract a Message from a string, typically received from a stream.
 * The expected format is NMEA0183 or the N2K Actisense format.
 * Example of N2K:
 *   2020-06-21-08:12:31.400,2,127251,204,255,8,ff,74,2d,fd,ff,ff,ff,ff
 */
public class NMEAInputManager {

    private interface StringInputHandler {
        Message[] getSentences(String str);
    }

    private static final Message[] EMPTY = new Message[0];

    private static class N2KHandlerExp implements StringInputHandler {
        private final N2KStream stream;
        private final Log logger;

        N2KHandlerExp(Log logger) {
            this.logger = logger;
            stream = ThingsFactory.getInstance(N2KStream.class, logger);
        }

        @Override
        public Message[] getSentences(String pgn) {
            try {
                N2KMessage msg = stream.getMessage(pgn);
                if (msg!=null) return new Message[] {msg};
            } catch (Exception e) {
                logger.warning(String.format("Cannot parse n2k sentence {%s} {%s}", pgn, e.getMessage()));
            }
            return EMPTY;
        }

    }

    private static class NMEA0183Handler implements StringInputHandler {

        private final Log logger;

        NMEA0183Handler(Log logger) {
            this.logger = logger;
        }

        @Override
        public Message[] getSentences(String sSentence) {
            try {
                Sentence s = SentenceFactory.getInstance().createParser(sSentence);
                return new Message[] {NMEA0183Message.get(s)};
            } catch (Exception e) {
                logger.debug("Can't read NMEA sentence {" + sSentence + "} {" + e + "}");
            }
            return EMPTY;
        }
    }

    private final Log logger;
    private final StringInputHandler n2kHandler;
    private final StringInputHandler nmeaHandler;

    public NMEAInputManager(@NotNull Log logger) {
        n2kHandler = new N2KHandlerExp(logger);
        nmeaHandler = new NMEA0183Handler(logger);
        this.logger = logger;
    }

    public Message[] getMessage(String string) {
        if (string.charAt(0) == '$' /* regular NMEA0183 */ || string.charAt(0) == '!' /* AIS extension to NMEA0183 */ ) {
            return nmeaHandler.getSentences(string);
        } else if (string.charAt(0) >= '1' && string.charAt(0) <= '2') {
            return n2kHandler.getSentences(string);
        } else {
            logger.debug("Cannot find a suitable handler for {" + string + "}");
            return EMPTY;
        }
    }
}
