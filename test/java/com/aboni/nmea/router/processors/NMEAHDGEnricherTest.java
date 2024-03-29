/*
 * Copyright (c) 2020,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.processors;

import com.aboni.nmea.NMEAMessagesModule;
import com.aboni.nmea.router.data.DataEvent;
import com.aboni.nmea.message.Message;
import com.aboni.nmea.message.MsgHeading;
import com.aboni.nmea.message.MsgPosition;
import com.aboni.nmea.message.MsgSOGAdCOG;
import com.aboni.nmea.nmea0183.NMEA0183Message;
import com.aboni.nmea.nmea0183.NMEA0183MessageFactory;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.processors.NMEAHDGEnricher;
import com.aboni.nmea.router.processors.NMEARouterProcessorException;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.HDMSentence;
import net.sf.marineapi.nmea.sentence.HDTSentence;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.util.Position;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NMEAHDGEnricherTest {

    static long getNow() {
        return System.currentTimeMillis();
    }

    static class MyPosition implements MsgPosition {

        @Override
        public Position getPosition() {
            return new Position(43.68008333, 10.28983333);
        }
    }

    static class MyCache implements NMEACache {

        boolean hasPosition = true;

        MyCache(boolean hasPosition) {
            this.hasPosition = hasPosition;
        }

        MyCache() {
        }

        @Override
        public DataEvent<MsgHeading> getLastHeading() {
            return null;
        }

        @Override
        public DataEvent<MsgSOGAdCOG> getLastVector() {
            return null;
        }

        @Override
        public DataEvent<MsgPosition> getLastPosition() {
            return new DataEvent<>(new MyPosition(), getNow() - 1000, "SRCGPS");
        }

		@Override
		public boolean isHeadingOlderThan(long time, long threshold) { return false; }

		@Override
		public void onSentence(Message s, String src) {}

        @Override
        public <T> void setStatus(String statusKey, T status) {

        }

        @Override
        public <T> T getStatus(String statusKey, T defV) {
            return null;
        }
    }

    private NMEA0183MessageFactory msgFactory;

    @Before
    public void setUp() {
        Injector injector = Guice.createInjector(new NMEARouterModule(), new NMEAMessagesModule());
        ThingsFactory.setInjector(injector);
        msgFactory = ThingsFactory.getInstance(NMEA0183MessageFactory.class);
    }

    @Test
    public void testEnrichVariation() throws NMEARouterProcessorException {
        NMEAHDGEnricher filler = new NMEAHDGEnricher(new MyCache(), false, false, 2016);

        HDGSentence hdg = (HDGSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
        hdg.setHeading(320.0);
        filler.process(msgFactory.getMessage(hdg), "SRC");
        assertEquals(2.5, hdg.getVariation(), 0.1);
    }

    @Test
    public void testEnrichVariationFail() throws NMEARouterProcessorException {
        NMEAHDGEnricher filler = new NMEAHDGEnricher(new MyCache(false), false, false, 2016);

        HDGSentence hdg = (HDGSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
        hdg.setHeading(320.0);
        filler.process(msgFactory.getMessage(hdg), "SRC");
        assertEquals(2.5, hdg.getVariation(), 0.1);
    }

    @Test
    public void testHDT() throws NMEARouterProcessorException {
        NMEAHDGEnricher filler = new NMEAHDGEnricher(new MyCache(), false, true, 2016);

        HDGSentence hdg = (HDGSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
        hdg.setHeading(320.0);
        Message[] res = filler.process(msgFactory.getMessage(hdg), "SRC").second;
        assertEquals(1, res.length);
        HDTSentence hdt = (HDTSentence) ((NMEA0183Message)res[0]).getSentence();
        assertEquals(322.5, hdt.getHeading(), 0.1);
    }

    @Test
    public void testHDM() throws NMEARouterProcessorException {
        NMEAHDGEnricher filler = new NMEAHDGEnricher(new MyCache(), true, false, 2016);

        HDGSentence hdg = (HDGSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
        hdg.setHeading(320.0);
        Message[] res = filler.process(msgFactory.getMessage(hdg), "SRC").second;
        assertEquals(1, res.length);
        HDMSentence hdm = (HDMSentence) ((NMEA0183Message)res[0]).getSentence();
        assertEquals(320.0, hdm.getHeading(), 0.1);
    }
}
