package com.aboni.nmea.router.filters;

import static org.junit.Assert.*;

import org.junit.Test;

import com.aboni.nmea.router.processors.NMEAChangeTalkerProcessor;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TalkerId;

public class NMEAChangeTalkerProcessorTest {

	@Test
	public void testNoChange() {
		NMEAChangeTalkerProcessor p = new NMEAChangeTalkerProcessor(TalkerId.GP, TalkerId.II);
		Sentence s = SentenceFactory.getInstance().createParser(TalkerId.II, "GLL");
		Sentence[] s1 = p.process(s, "SRC");
		assertEquals(1, s1.length);
		assertEquals(TalkerId.II, s1[0].getTalkerId());
	}
	
	@Test
	public void testNoChange1() {
		NMEAChangeTalkerProcessor p = new NMEAChangeTalkerProcessor(TalkerId.GP, TalkerId.II);
		Sentence s = SentenceFactory.getInstance().createParser(TalkerId.AB, "GLL");
		Sentence[] s1 = p.process(s, "SRC");
		assertEquals(1, s1.length);
		assertEquals(TalkerId.AB, s1[0].getTalkerId());
	}

	@Test
	public void testChange() {
		NMEAChangeTalkerProcessor p = new NMEAChangeTalkerProcessor(TalkerId.GP, TalkerId.II);
		Sentence s = SentenceFactory.getInstance().createParser(TalkerId.GP, "GLL");
		Sentence[] s1 = p.process(s, "SRC");
		assertEquals(1, s1.length);
		assertEquals(TalkerId.II, s1[0].getTalkerId());
	}

}
