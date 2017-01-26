package com.aboni.nmea.router.filters;

import static org.junit.Assert.*;


import org.junit.Test;

import com.aboni.nmea.router.filters.NMEAFilterSet.TYPE;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TalkerId;

public class NMEAFilterSetTest {

	@Test
	public void testEmptyWhiteList() {
		NMEAFilterSet set = new NMEAFilterSet(TYPE.WHITELIST);
		Sentence test = SentenceFactory.getInstance().createParser(TalkerId.II, "GLL");
		// any sentence fails
		assertTrue(!set.match(test, "WHATEVER"));
	}
	

	@Test
	public void testWhiteList() {
		NMEABasicSentenceFilter f1 = new NMEABasicSentenceFilter("GLL");
		NMEABasicSentenceFilter f2 = new NMEABasicSentenceFilter("HDG");
		NMEAFilterSet set = new NMEAFilterSet(TYPE.WHITELIST);
		set.addFilter(f1);
		set.addFilter(f2);

		Sentence test = SentenceFactory.getInstance().createParser(TalkerId.II, "GLL");
		assertTrue(set.match(test, "WHATEVER"));
		
		test = SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
		assertTrue(set.match(test, "WHATEVER"));
		
		test = SentenceFactory.getInstance().createParser(TalkerId.II, "GGA");
		assertTrue(!set.match(test, "WHATEVER"));
	}


	@Test
	public void testEmptyBlackList() {
		NMEAFilterSet set = new NMEAFilterSet(TYPE.BLACKLIST);
		Sentence test = SentenceFactory.getInstance().createParser(TalkerId.II, "GLL");
		assertTrue(set.match(test, "WHATEVER"));
	}
	
	@Test
	public void testBlackList() {
		NMEABasicSentenceFilter f1 = new NMEABasicSentenceFilter("GLL");
		NMEABasicSentenceFilter f2 = new NMEABasicSentenceFilter("HDG");
		NMEAFilterSet set = new NMEAFilterSet(TYPE.BLACKLIST);
		set.addFilter(f1);
		set.addFilter(f2);

		Sentence test = SentenceFactory.getInstance().createParser(TalkerId.II, "GLL");
		assertTrue(!set.match(test, "WHATEVER"));
		
		test = SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
		assertTrue(!set.match(test, "WHATEVER"));
		
		test = SentenceFactory.getInstance().createParser(TalkerId.II, "GGA");
		assertTrue(set.match(test, "WHATEVER"));
	}

}
