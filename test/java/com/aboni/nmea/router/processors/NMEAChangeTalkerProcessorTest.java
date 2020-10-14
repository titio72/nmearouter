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

import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.nmea0183.NMEA0183Message;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TalkerId;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NMEAChangeTalkerProcessorTest {

	@Test
	public void testNoChange() {
		NMEAChangeTalkerProcessor p = new NMEAChangeTalkerProcessor(TalkerId.GP, TalkerId.II);
		Sentence s = SentenceFactory.getInstance().createParser(TalkerId.II, "GLL");
		Message[] s1 = p.process(NMEA0183Message.get(s), "SRC").second;
		assertEquals(0, s1.length);
		assertEquals(TalkerId.II, s.getTalkerId());
	}
	
	@Test
	public void testNoChange1() {
		NMEAChangeTalkerProcessor p = new NMEAChangeTalkerProcessor(TalkerId.GP, TalkerId.II);
		Sentence s = SentenceFactory.getInstance().createParser(TalkerId.AB, "GLL");
		Message[] s1 = p.process(NMEA0183Message.get(s), "SRC").second;
		assertEquals(1, s1.length);
		assertEquals(TalkerId.AB, ((NMEA0183Message)s1[0]).getSentence().getTalkerId());
	}

	@Test
	public void testChange() {
		NMEAChangeTalkerProcessor p = new NMEAChangeTalkerProcessor(TalkerId.GP, TalkerId.II);
		Sentence s = SentenceFactory.getInstance().createParser(TalkerId.GP, "GLL");
		Message[] s1 = p.process(NMEA0183Message.get(s), "SRC").second;
		assertEquals(1, s1.length);
		assertEquals(TalkerId.II,  ((NMEA0183Message)s1[0]).getSentence().getTalkerId());
	}

}
