package com.aboni.nmea.router.streamer;

import com.aboni.nmea.sentences.NMEASentenceItem;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NMEASentenceItemTest {

	@Before
	public void setUp() {
	}

	@Test
	public void testOut() {
		String s = "$GPMTW,28.50,C*3B";
		NMEASentenceItem itm = new NMEASentenceItem(SentenceFactory.getInstance().createParser(s), 1485070801108L, "**");
		assertEquals(1485070801108L, itm.getTimestamp());
		assertEquals("**", itm.getData());
		assertEquals(s, itm.getSentence().toSentence());
		assertEquals("[1485070801108][**] $GPMTW,28.50,C*3B", itm.toString());
	}

	@Test
	public void testIn() throws Exception {
		NMEASentenceItem itm = new NMEASentenceItem("[1485070801108][**] $GPMTW,28.50,C*3B");
		assertEquals(1485070801108L, itm.getTimestamp());
		assertEquals("**", itm.getData());
		assertEquals("$GPMTW,28.50,C*3B", itm.getSentence().toSentence());
	}
}
