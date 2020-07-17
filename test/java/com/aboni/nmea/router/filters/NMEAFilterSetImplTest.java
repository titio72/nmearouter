package com.aboni.nmea.router.filters;

import com.aboni.nmea.router.filters.impl.NMEABasicSentenceFilter;
import com.aboni.nmea.router.filters.impl.NMEAFilterSetImpl;
import com.aboni.nmea.router.filters.impl.NMEAFilterSetImpl.TYPE;
import com.aboni.nmea.router.filters.impl.STalkFilter;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TalkerId;
import org.junit.Test;

import static org.junit.Assert.*;

public class NMEAFilterSetImplTest {

    @Test
    public void testEmptyWhiteList() {
        NMEAFilterSetImpl set = new NMEAFilterSetImpl(TYPE.WHITELIST);
        Sentence test = SentenceFactory.getInstance().createParser(TalkerId.II, "GLL");
        // any sentence fails
        assertFalse(set.match(test, "WHATEVER"));
    }
	

	@Test
	public void testWhiteList() {
        NMEABasicSentenceFilter f1 = new NMEABasicSentenceFilter("GLL");
        NMEABasicSentenceFilter f2 = new NMEABasicSentenceFilter("HDG");
        NMEAFilterSetImpl set = new NMEAFilterSetImpl(TYPE.WHITELIST);
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
        NMEAFilterSetImpl set = new NMEAFilterSetImpl(TYPE.BLACKLIST);
        Sentence test = SentenceFactory.getInstance().createParser(TalkerId.II, "GLL");
        assertTrue(set.match(test, "WHATEVER"));
    }
	
	@Test
	public void testBlackList() {
        NMEABasicSentenceFilter f1 = new NMEABasicSentenceFilter("GLL");
        NMEABasicSentenceFilter f2 = new NMEABasicSentenceFilter("HDG");
        NMEAFilterSetImpl set = new NMEAFilterSetImpl(TYPE.BLACKLIST);
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
    public void testCount() {
        NMEABasicSentenceFilter f1 = new NMEABasicSentenceFilter("GLL");
        NMEABasicSentenceFilter f2 = new NMEABasicSentenceFilter("HDG");
        NMEAFilterSetImpl set = new NMEAFilterSetImpl(TYPE.BLACKLIST);
        assertEquals(0, set.count());
        set.addFilter(f1);
        set.addFilter(f2);
        assertEquals(2, set.count());
    }

    @Test
    public void testDrop1() {
        NMEABasicSentenceFilter f1 = new NMEABasicSentenceFilter("GLL");
        NMEABasicSentenceFilter f2 = new NMEABasicSentenceFilter("HDG");
        NMEAFilterSetImpl set = new NMEAFilterSetImpl(TYPE.BLACKLIST);
        assertEquals(0, set.count());
        set.addFilter(f1);
        set.addFilter(f2);
        set.dropFilter(f1);
        assertEquals(1, set.count());
        assertEquals(f2, set.getFilters().next());
    }

    @Test
    public void testDrop2() {
        NMEABasicSentenceFilter f1 = new NMEABasicSentenceFilter("GLL");
        NMEABasicSentenceFilter f2 = new NMEABasicSentenceFilter("HDG");
        NMEAFilterSetImpl set = new NMEAFilterSetImpl(TYPE.BLACKLIST);
        assertEquals(0, set.count());
        set.addFilter(f1);
        set.addFilter(f2);
        set.dropFilter(f2);
        assertEquals(1, set.count());
        assertEquals(f1, set.getFilters().next());
    }

    @Test
    public void testDrop0() {
        NMEAFilterSetImpl set = new NMEAFilterSetImpl(TYPE.BLACKLIST);
        assertEquals(0, set.count());
        set.dropFilter(new NMEABasicSentenceFilter("GLL"));
        assertEquals(0, set.count());
    }

    @Test
    public void testBlackListSTalk() {
        STalkFilter f1 = new STalkFilter("84", true);
        NMEAFilterSetImpl set = new NMEAFilterSetImpl(TYPE.BLACKLIST);
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
