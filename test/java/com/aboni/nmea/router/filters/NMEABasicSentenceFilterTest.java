package com.aboni.nmea.router.filters;

import static org.junit.Assert.*;

import org.junit.Test;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;

public class NMEABasicSentenceFilterTest {

	@Test
	public void testSentenceType() {
		NMEABasicSentenceFilter f = new NMEABasicSentenceFilter("GLL", null, "");
		
		Sentence sMatch = SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.GLL);
		assertTrue(f.match(sMatch, "whatever"));

		Sentence sNoMatch = SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.GGA);
		assertTrue(!f.match(sNoMatch, "whatever"));
	}

	@Test
	public void testSentenceTypeAndSource() {
		NMEABasicSentenceFilter f = new NMEABasicSentenceFilter("GLL", null, "SRC");
		
		Sentence sMatch = SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.GLL);
		assertTrue(f.match(sMatch, "SRC"));
		assertTrue(!f.match(sMatch, "ANOTHERSRC"));

		Sentence sNoMatch = SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.GGA);
		assertTrue(!f.match(sNoMatch, "SRC"));
		assertTrue(!f.match(sNoMatch, "ANOTHERSRC"));
	}

	@Test
	public void testTalkerId() {
		NMEABasicSentenceFilter f = new NMEABasicSentenceFilter("", TalkerId.II, "");
		Sentence s = SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.GLL);
		assertTrue(f.match(s, "whatever"));

		Sentence s1 = SentenceFactory.getInstance().createParser(TalkerId.GP, SentenceId.GLL);
		assertTrue(!f.match(s1, "whatever"));
	}

	@Test
	public void testSource() {
		NMEABasicSentenceFilter f = new NMEABasicSentenceFilter("", null, "SRC");
		Sentence s = SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.GLL);
		assertTrue(f.match(s, "SRC"));
		assertTrue(!f.match(s, "ANOTHERSRC"));
	}

}
