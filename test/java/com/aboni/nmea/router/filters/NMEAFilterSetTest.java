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
        assertFalse(set.match(test, "WHATEVER"));
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
		assertFalse(set.match(test, "WHATEVER"));
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
		assertFalse(set.match(test, "WHATEVER"));
		
		test = SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
		assertFalse(set.match(test, "WHATEVER"));
		
		test = SentenceFactory.getInstance().createParser(TalkerId.II, "GGA");
		assertTrue(set.match(test, "WHATEVER"));
	}
	
	@Test
	public void testBlackListSTalk() {
		STalkFilter f1 = new STalkFilter("84", true);
		NMEAFilterSet set = new NMEAFilterSet(TYPE.BLACKLIST);
		set.addFilter(f1);

		//should pass any sentence not-STALK and STALK:84
		
		Sentence test = SentenceFactory.getInstance().createParser("$STALK,84,36,85,88,40,00,0A,02,08*16");
		assertTrue(set.match(test, "WHATEVER"));
		
		test = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A*0B");
		assertTrue(set.match(test, "WHATEVER"));

		test = SentenceFactory.getInstance().createParser("$STALK,23,01,0E,39*3E");
		assertFalse(set.match(test, "WHATEVER"));
	}

}
