package com.aboni.nmea.router.filters;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;
import org.junit.Test;

import static org.junit.Assert.*;

public class NMEABasicSentenceFilterTest {

	@Test
	public void testSentenceType() {
        NMEABasicSentenceFilter f = new NMEABasicSentenceFilter("GLL");
        assertEquals("GLL", f.getSentenceId());

        Sentence sMatch = SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.GLL);
        assertTrue(f.match(sMatch, "whatever"));

        Sentence sNoMatch = SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.GGA);
        assertFalse(f.match(sNoMatch, "whatever"));
    }

	@Test
	public void testSentenceTypeAndSource() {
        NMEABasicSentenceFilter f = new NMEABasicSentenceFilter("GLL", "SRC");
        assertEquals("SRC", f.getSource());
        assertEquals("GLL", f.getSentenceId());

        Sentence sMatch = SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.GLL);
        assertTrue(f.match(sMatch, "SRC"));
        assertFalse(f.match(sMatch, "ANOTHERSRC"));

        Sentence sNoMatch = SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.GGA);
        assertFalse(f.match(sNoMatch, "SRC"));
        assertFalse(f.match(sNoMatch, "ANOTHERSRC"));
    }

	@Test
	public void testTalkerId() {
        NMEABasicSentenceFilter f = new NMEABasicSentenceFilter("", TalkerId.II, "");
        assertEquals(TalkerId.II, f.getTalkerId());

        Sentence s = SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.GLL);
        assertTrue(f.match(s, "whatever"));

        Sentence s1 = SentenceFactory.getInstance().createParser(TalkerId.GP, SentenceId.GLL);
        assertFalse(f.match(s1, "whatever"));
    }

	@Test
	public void testSource() {
		NMEABasicSentenceFilter f = new NMEABasicSentenceFilter("", null, "SRC");
		Sentence s = SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.GLL);
		assertTrue(f.match(s, "SRC"));
		assertFalse(f.match(s, "ANOTHERSRC"));
	}

}
