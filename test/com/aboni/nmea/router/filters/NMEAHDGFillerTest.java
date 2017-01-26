package com.aboni.nmea.router.filters;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

import com.aboni.nmea.router.NMEACache;
import com.aboni.utils.DataEvent;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.GLLSentence;
import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.HDMSentence;
import net.sf.marineapi.nmea.sentence.HDTSentence;
import net.sf.marineapi.nmea.sentence.HeadingSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.util.Measurement;
import net.sf.marineapi.nmea.util.Position;

public class NMEAHDGFillerTest {

	class MyCache implements NMEACache {

		boolean hasPosition = true;
		
		MyCache(boolean hasPosition) {
			this.hasPosition = hasPosition;
		}
		
		MyCache() {}
		
		@Override
		public boolean isStarted() { return true; }

		@Override
		public DataEvent<HeadingSentence> getLastHeading() { return null; }

		@Override
		public DataEvent<PositionSentence> getLastPosition() { 
			GLLSentence gll = (GLLSentence)SentenceFactory.getInstance().createParser(TalkerId.GP, "GLL");
			gll.setPosition(new Position(43.68008333, 10.28983333));
			DataEvent<PositionSentence> e = new DataEvent<PositionSentence>();
			e.source = "SRCGPS";
			e.timestamp = System.currentTimeMillis() - 1000;
			e.data = gll;
			return e;
		}
		
		@Override
		public DataEvent<Measurement> getSensorData(String sensorName) { return null; }

		@Override
		public Collection<String> getSensors() { return null; }

		@Override
		public boolean isHeadingOlderThan(long time, long threshold) { return false; }

		@Override
		public boolean isPositionOlderThan(long time, long threshold) { return false; }
		
	}
	
	@Test
	public void testEnrichVariation() {
		NMEAHDGFiller filler = new NMEAHDGFiller(false, false, new MyCache());

		HDGSentence hdg = (HDGSentence)SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
		hdg.setHeading(320.0);
		filler.process(hdg, "SRC");
		assertEquals(2.5, hdg.getVariation(), 0.1);
	}

	@Test
	public void testEnrichVariationFail() {
		NMEAHDGFiller filler = new NMEAHDGFiller(false, false, new MyCache(false));

		HDGSentence hdg = (HDGSentence)SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
		hdg.setHeading(320.0);
		filler.process(hdg, "SRC");
		assertEquals(2.5, hdg.getVariation(), 0.1);
	}

	@Test
	public void testHDT() {
		NMEAHDGFiller filler = new NMEAHDGFiller(false, true, new MyCache());

		HDGSentence hdg = (HDGSentence)SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
		hdg.setHeading(320.0);
		Sentence[] res = filler.process(hdg, "SRC");
		assertEquals(1, res.length);
		HDTSentence hdt = (HDTSentence)res[0];
		assertEquals(322.5, hdt.getHeading(), 0.1);
	}

	@Test
	public void testHDM() {
		NMEAHDGFiller filler = new NMEAHDGFiller(true, false, new MyCache());

		HDGSentence hdg = (HDGSentence)SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
		hdg.setHeading(320.0);
		Sentence[] res = filler.process(hdg, "SRC");
		assertEquals(1, res.length);
		HDMSentence hdm = (HDMSentence)res[0];
		assertEquals(320.0, hdm.getHeading(), 0.1);
	}

}
