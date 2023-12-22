package com.aboni.nmea.router.filters.impl;

import com.aboni.nmea.nmea0183.impl.NMEA0183MessageFactoryImpl;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.impl.RouterMessageFactoryImpl;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class STalkFilterTest {

    private static RouterMessage getRouterMessage(Sentence test) {
        return new RouterMessageFactoryImpl(new NMEA0183MessageFactoryImpl()).createMessage(test, "WHATEVER", System.currentTimeMillis());
    }

	@Test
	public void testNegateFalse() {
        STalkFilter f = new STalkFilter("84", true);
        Sentence s = SentenceFactory.getInstance().createParser("$STALK,23,01,0E,39*3E");
        // accept because it is not a 84
        assertTrue(f.match(getRouterMessage(s)));
    }

	@Test
	public void testNegateTrue() {
        STalkFilter f = new STalkFilter("84", true);
        Sentence s = SentenceFactory.getInstance().createParser("$STALK,84,36,85,88,40,00,0A,02,08*16");
        // reject because is a 84
        assertFalse(f.match(getRouterMessage(s)));
    }

	@Test
	public void testFalse() {
        STalkFilter f = new STalkFilter("84", false);
        Sentence s = SentenceFactory.getInstance().createParser("$STALK,23,01,0E,39*3E");
        // accept because it is not a 84
        assertFalse(f.match(getRouterMessage(s)));
    }

	@Test
	public void testTrue() {
        STalkFilter f = new STalkFilter("84", false);
        Sentence s = SentenceFactory.getInstance().createParser("$STALK,84,36,85,88,40,00,0A,02,08*16");
        // reject because is a 84
        assertTrue(f.match(getRouterMessage(s)));
    }

	@Test
	public void testNotSTalk() {
        STalkFilter f = new STalkFilter("84", false);
        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A*0B");
        assertFalse(f.match(getRouterMessage(s)));
    }

	@Test
	public void testNegateNotSTalk() {
        STalkFilter f = new STalkFilter("84", true);
        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A*0B");
        assertFalse(f.match(getRouterMessage(s)));
    }
}
