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

package com.aboni.nmea.router.processors;

import com.aboni.geo.NMEAMagnetic2TrueConverter;
import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.message.MsgPosition;
import com.aboni.nmea.router.nmea0183.NMEA0183Message;
import com.aboni.nmea.router.utils.DataEvent;
import com.aboni.utils.Pair;
import com.aboni.utils.Utils;
import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.Position;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

/**
 * Enrich HDG heading information:
 * 1) Listen to GPS location to set the magnetic variation into the HDG sentence (if not present)
 * 2) Split the sentence in HDM & HDT   
 * @author aboni
 *
 */
public class NMEAHDGEnricher implements NMEAPostProcess {

    private final NMEAMagnetic2TrueConverter m;

    private final boolean doHDM;
    private final boolean doHDT;
    private final NMEACache cache;

    @Inject
    public NMEAHDGEnricher(@NotNull NMEACache cache) {
        this(cache, true, true);
    }

    public NMEAHDGEnricher(NMEACache cache, boolean hdm, boolean hdt, int year) {
        m = new NMEAMagnetic2TrueConverter(year, Logger.getLogger(Constants.LOG_CONTEXT), Constants.WMM);
        this.cache = cache;
        this.doHDM = hdm;
        this.doHDT = hdt;
    }

    public NMEAHDGEnricher(NMEACache cache, boolean hdm, boolean hdt) {
        this(cache, hdm, hdt, Calendar.getInstance().get(Calendar.YEAR));
    }

    private static <T extends Sentence> boolean isA(Class<T> type, Message m) {
        return m instanceof NMEA0183Message &&
                type.isInstance(((NMEA0183Message) m).getSentence());
    }

    @Override
    public Pair<Boolean, Message[]> process(Message message, String src) throws NMEARouterProcessorException {
        try {
            if (isA(HDGSentence.class, message)) {
                HDGSentence hdg = (HDGSentence) ((NMEA0183Message) message).getSentence();
                List<Message> out = new ArrayList<>(2);
                boolean canDoT = fillVariation(hdg, getLastPosition());
                if (doHDM) {
                    out.add(getHDM(hdg));
                }
                if (doHDT && canDoT) {
                    out.add(getHDT(hdg));
                }

                return new Pair<>(Boolean.TRUE, out.toArray(new Message[0]));
            } else if ((doHDM && isA(HDMSentence.class, message))
                    || (doHDT && isA(HDTSentence.class, message))) {
                // skip HDT & HDM if they are supposed to be produced by the enricher
                return new Pair<>(Boolean.FALSE, new Message[]{});
            }
        } catch (Exception e) {
            throw new NMEARouterProcessorException("Cannot enrich heading \"" + message + "\"", e);
        }
        return new Pair<>(Boolean.TRUE, null);
    }

    private boolean fillVariation(HDGSentence hdg, Position lastPosition) {
        boolean canDoT = false;
        try {
            hdg.getVariation();
            canDoT = true;
        } catch (DataNotAvailableException e) {
            if (lastPosition!=null) {
                double d = m.getDeclination(lastPosition);
                d = Utils.normalizeDegrees180To180(d);
                hdg.setVariation(d);
                canDoT = true;
            }
        }
        return canDoT;
    }

    private Position getLastPosition() {
        Position lastPosition = null;
        DataEvent<MsgPosition> ev = cache.getLastPosition();
        if (ev!=null && ev.getData()!=null) {
            lastPosition = ev.getData().getPosition();
        }
        return lastPosition;
    }

    private Message getHDM(HDGSentence hdg) {
        HDMSentence hdm = (HDMSentence) SentenceFactory.getInstance().createParser(hdg.getTalkerId(), SentenceId.HDM);
        hdm.setHeading(hdg.getHeading());
        return new NMEA0183Message(hdm);
    }

    private Message getHDT(HDGSentence hdg) {
        HDTSentence hdt = (HDTSentence) SentenceFactory.getInstance().createParser(hdg.getTalkerId(), SentenceId.HDT);
        double variation;
        double deviation;
        try {
            variation = hdg.getVariation();
        } catch (DataNotAvailableException e) {
            variation = 0.0;
        }
        try {
            deviation = hdg.getDeviation();
        } catch (DataNotAvailableException e) {
            deviation = 0.0;
        }
        hdt.setHeading(Utils.normalizeDegrees0To360(hdg.getHeading() + variation + deviation));
        return new NMEA0183Message(hdt);
    }

    @Override
    public void onTimer() {
        // nothing to do
    }

}
