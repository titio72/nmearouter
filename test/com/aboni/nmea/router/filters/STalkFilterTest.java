package com.aboni.nmea.router.filters;

import static org.junit.Assert.*;

import org.junit.Test;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

public class STalkFilterTest {

	@Test
	public void testNegateFalse() {
		STalkFilter f = new STalkFilter("84", true);
		Sentence s = SentenceFactory.getInstance().createParser("$STALK,23,01,0E,39*3E");
		// accept because it is not a 84
		assertTrue(f.match(s, "XXX"));
	}

	@Test
	public void testNegateTrue() {
		STalkFilter f = new STalkFilter("84", true);
		Sentence s = SentenceFactory.getInstance().createParser("$STALK,84,36,85,88,40,00,0A,02,08*16");
		// reject because is a 84
		assertFalse(f.match(s, "XXX"));
	}

	@Test
	public void testFalse() {
		STalkFilter f = new STalkFilter("84", false);
		Sentence s = SentenceFactory.getInstance().createParser("$STALK,23,01,0E,39*3E");
		// accept because it is not a 84
		assertFalse(f.match(s, "XXX"));
	}

	@Test
	public void testTrue() {
		STalkFilter f = new STalkFilter("84", false);
		Sentence s = SentenceFactory.getInstance().createParser("$STALK,84,36,85,88,40,00,0A,02,08*16");
		// reject because is a 84
		assertTrue(f.match(s, "XXX"));
	}

	@Test
	public void testNotSTalk() {
		STalkFilter f = new STalkFilter("84", false);
		Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A*0B");
		assertFalse(f.match(s, "XXX"));
	}

	@Test
	public void testNegateNotSTalk() {
		STalkFilter f = new STalkFilter("84", true);
		Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A*0B");
		assertFalse(f.match(s, "XXX"));
	}
}
