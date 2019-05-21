package com.aboni.nmea.router.filters;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.processors.NMEAHDGEnricher;
import com.aboni.utils.DataEvent;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.Position;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NMEAHDGEnricherTest {

	class MyCache implements NMEACache {

		boolean hasPosition = true;
		
		MyCache(boolean hasPosition) {
			this.hasPosition = hasPosition;
		}
		
		MyCache() {}
		
		@Override
		public DataEvent<HeadingSentence> getLastHeading() { return null; }

		@Override
		public DataEvent<PositionSentence> getLastPosition() { 
			GLLSentence gll = (GLLSentence)SentenceFactory.getInstance().createParser(TalkerId.GP, "GLL");
			gll.setPosition(new Position(43.68008333, 10.28983333));
			return new DataEvent<>(gll, System.currentTimeMillis() - 1000, "SRCGPS");
		}

		@Override
		public boolean isHeadingOlderThan(long time, long threshold) { return false; }

		@Override
		public void onSentence(Sentence s, String src) {}

		@Override
		public boolean isTimeSynced() {
			return true;
		}

		@Override
		public void setTimeSynced() {
		}

		public long getTimeSkew() {
			return 0;
		}
		
	}
	
	@Test
	public void testEnrichVariation() {
		NMEAHDGEnricher filler = new NMEAHDGEnricher(new MyCache(), false, false);

		HDGSentence hdg = (HDGSentence)SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
		hdg.setHeading(320.0);
		filler.process(hdg, "SRC");
		assertEquals(2.5, hdg.getVariation(), 0.1);
	}

	@Test
	public void testEnrichVariationFail() {
		NMEAHDGEnricher filler = new NMEAHDGEnricher(new MyCache(false), false, false);

		HDGSentence hdg = (HDGSentence)SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
		hdg.setHeading(320.0);
		filler.process(hdg, "SRC");
		assertEquals(2.5, hdg.getVariation(), 0.1);
	}

	@Test
	public void testHDT() {
		NMEAHDGEnricher filler = new NMEAHDGEnricher(new MyCache(), false, true);

		HDGSentence hdg = (HDGSentence)SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
		hdg.setHeading(320.0);
		Sentence[] res = filler.process(hdg, "SRC").second;
		assertEquals(1, res.length);
		HDTSentence hdt = (HDTSentence)res[0];
		assertEquals(322.5, hdt.getHeading(), 0.1);
	}

	@Test
	public void testHDM() {
		NMEAHDGEnricher filler = new NMEAHDGEnricher(new MyCache(), true, false);

		HDGSentence hdg = (HDGSentence)SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
		hdg.setHeading(320.0);
		Sentence[] res = filler.process(hdg, "SRC").second;
		assertEquals(1, res.length);
		HDMSentence hdm = (HDMSentence)res[0];
		assertEquals(320.0, hdm.getHeading(), 0.1);
	}

}
