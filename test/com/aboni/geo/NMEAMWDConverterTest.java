package com.aboni.geo;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.MWDSentence;
import net.sf.marineapi.nmea.sentence.MWVSentence;
import net.sf.marineapi.nmea.sentence.TalkerId;

public class NMEAMWDConverterTest {

	@Before
	public void setUp() throws Exception {
	}

	private HDGSentence getHeading(double heading, double variation, double deviation) {
		HDGSentence h = (HDGSentence)SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
		h.setHeading(heading);
		if (!Double.isNaN(variation)) h.setVariation(variation);
		if (!Double.isNaN(deviation)) h.setDeviation(deviation);
		return h;
	}

	private MWVSentence getWind(double speed, boolean tWind, double angle) {
		MWVSentence w = (MWVSentence)SentenceFactory.getInstance().createParser(TalkerId.II, "MWV");
		w.setAngle(angle);
		w.setTrue(tWind);
		w.setSpeed(speed);
		return w;
	}
	
	@Test
	public void test() {
		NMEAMWDConverter c = new NMEAMWDConverter(TalkerId.II);
		c.setHeading(getHeading(320, 2.5, 0.5));
		c.setWind(getWind(20.0, true, 35.0));
		MWDSentence m = c.getMWDSentence();
		
		assertEquals(20.0, m.getWindSpeedKnots(), 0.001);
		assertEquals(355.5, m.getMagneticWindDirection(), 0.001);
		assertEquals(358.0, m.getTrueWindDirection(), 0.001);
	}

	@Test
	public void testNoDeviation() {
		NMEAMWDConverter c = new NMEAMWDConverter(TalkerId.II);
		c.setHeading(getHeading(320, 2.5, Double.NaN));
		c.setWind(getWind(20.0, true, 35.0));
		MWDSentence m = c.getMWDSentence();
		
		assertEquals(20.0, m.getWindSpeedKnots(), 0.001);
		assertEquals(320 + 35, 			m.getMagneticWindDirection(), 0.001);
		assertEquals(320 + 35 + 2.5, 	m.getTrueWindDirection(), 0.001);
	}

	@Test
	public void testNoDeclination() {
		NMEAMWDConverter c = new NMEAMWDConverter(TalkerId.II);
		c.setHeading(getHeading(320, Double.NaN, Double.NaN));
		c.setWind(getWind(20.0, true, 35.0));
		MWDSentence m = c.getMWDSentence();
		
		assertEquals(20.0, m.getWindSpeedKnots(), 0.001);
		assertEquals(320 + 35, 	m.getMagneticWindDirection(), 0.001);
		assertEquals(320 + 35, 	m.getTrueWindDirection(), 0.001);
	}

	@Test
	public void testNoWind() {
		NMEAMWDConverter c = new NMEAMWDConverter(TalkerId.II);
		c.setHeading(getHeading(320, Double.NaN, Double.NaN));
		MWDSentence m = c.getMWDSentence();
		assertNull(m);
	}

	@Test
	public void testNoHeading() {
		NMEAMWDConverter c = new NMEAMWDConverter(TalkerId.II);
		c.setWind(getWind(20.0, true, 35.0));
		MWDSentence m = c.getMWDSentence();
		assertNull(m);
	}
	
	@Test
	public void testTooOld() {
		NMEAMWDConverter c = new NMEAMWDConverter(TalkerId.II);
		c.setWind(getWind(20.0, true, 35.0), 1000);
		c.setHeading(getHeading(320, Double.NaN, Double.NaN), 11000);
		MWDSentence m = c.getMWDSentence(5000); // wind and heading cannot be more than 5s apart
		assertNull(m);
	}
	
	@Test
	public void testYoungEnough() {
		NMEAMWDConverter c = new NMEAMWDConverter(TalkerId.II);
		c.setWind(getWind(20.0, true, 35.0), 1000);
		c.setHeading(getHeading(320, Double.NaN, Double.NaN), 3000);
		MWDSentence m = c.getMWDSentence(5000); // wind and heading cannot be more than 5s apart
		assertNotNull(m);
	}
	
	

}
