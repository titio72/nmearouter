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
import com.aboni.utils.TimestampProvider;
import com.aboni.nmea.message.Message;
import com.aboni.nmea.message.MsgHeading;
import com.aboni.nmea.message.MsgPosition;
import com.aboni.nmea.message.impl.MsgHeadingImpl;
import com.aboni.nmea.nmea0183.NMEA0183Message;
import com.aboni.nmea.router.data.DataEvent;
import com.aboni.data.Pair;
import com.aboni.utils.Utils;
import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.Position;

import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * To be used when there's only HDM in the stream and one needs to enrich it with variation and deviation.
 * Enrich HDM heading information and produces additional HDT & HDG sentences
 *
 * @author aboni
 */
public class NMEAHDMEnricher implements NMEAPostProcess {

    private final NMEAMagnetic2TrueConverter m;

    private final NMEACache cache;

    @Inject
    public NMEAHDMEnricher(NMEACache cache, TimestampProvider tp) {
        if (cache==null) throw new IllegalArgumentException("Cache is null");
        if (tp==null) throw new IllegalArgumentException("Timestamp provider is null");
        m = new NMEAMagnetic2TrueConverter(tp.getYear(), Logger.getLogger(Constants.LOG_CONTEXT), Constants.WMM);
        this.cache = cache;
    }

    @Override
    public Pair<Boolean, Message[]> process(Message message, String src) throws NMEARouterProcessorException {
        try {
            Pair<Boolean, Message[]> res = null;
            if (message instanceof NMEA0183Message) {
                res = handleNMEA0183((NMEA0183Message) message);
            } else if (message instanceof MsgHeading) {
                res = handleMessage((MsgHeading) message);
            }
            return res;
        } catch (Exception e) {
            throw new NMEARouterProcessorException("Cannot enrich heading process message \"" + message + "\"", e);
        }
    }

    private Pair<Boolean, Message[]> handleMessage(MsgHeading message) {
        if (!message.isTrueHeading()) {
            Message trueHead = enrich(message, getLastPosition());
            if (trueHead != null)
                return new Pair<>(Boolean.TRUE, new Message[]{trueHead});
        }
        return new Pair<>(Boolean.TRUE, new Message[]{});
    }

    private Pair<Boolean, Message[]> handleNMEA0183(NMEA0183Message message) {
        Sentence sentence = message.getSentence();
        if (sentence instanceof HDMSentence) {
            HDMSentence hdm = (HDMSentence) sentence;
            HDGSentence hdg = getHDG(hdm);
            if (fillVariation(hdg, getLastPosition())) {
                return new Pair<>(Boolean.TRUE, new Message[]{new NMEA0183Message(hdg), new NMEA0183Message(getHDT(hdg))});
            } else {
                return new Pair<>(Boolean.TRUE, new Message[]{new NMEA0183Message(hdg)});
            }
        } else if (sentence instanceof HDGSentence || sentence instanceof HDTSentence) {
            // skip HDG and HDT as they are produced by the enricher
            return new Pair<>(Boolean.FALSE, new Message[]{});
        }
        return null;
    }

    private MsgHeading enrich(MsgHeading hdg, Position lastPosition) {
        MsgHeading trueHead = null;
        if (Double.isNaN(hdg.getVariation()) && lastPosition != null) {
            double d = m.getDeclination(lastPosition);
            d = Utils.normalizeDegrees180To180(d);
            trueHead = new MsgHeadingImpl(Utils.normalizeDegrees0To360(hdg.getHeading() + d), d, false);
        }
        return trueHead;
    }

    private boolean fillVariation(HDGSentence hdg, Position lastPosition) {
        boolean variationAvailable = false;
        try {
            // if the variation is already present skip the enrichment
            hdg.getVariation();
            variationAvailable = true;
        } catch (DataNotAvailableException e) {
            if (lastPosition != null) {
                double d = m.getDeclination(lastPosition);
                d = Utils.normalizeDegrees180To180(d);
                hdg.setVariation(d);
                variationAvailable = true;
            }
        }
        return variationAvailable;
    }

    private Position getLastPosition() {
        Position lastPosition = null;
        DataEvent<MsgPosition> ev = cache.getLastPosition();
        if (ev!=null && ev.getData()!=null) {
            lastPosition = ev.getData().getPosition();
        }
        return lastPosition;
    }

    private HDGSentence getHDG(HDMSentence hdm) {
        HDGSentence hdg = (HDGSentence) SentenceFactory.getInstance().createParser(hdm.getTalkerId(), SentenceId.HDG);
        hdg.setHeading(hdm.getHeading());
        return hdg;
    }

    private HDTSentence getHDT(HDGSentence hdg) {
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
        return hdt;
    }

    @Override
    public void onTimer() {
        // nothing ot do
    }

}
