package com.aboni.nmea.router.filters.impl;

import com.aboni.nmea.nmea0183.impl.NMEA0183MessageFactoryImpl;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.filters.DummyFilter;
import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.nmea.router.filters.NMEAFilterSet;
import com.aboni.nmea.router.filters.NMEAFilterSet.TYPE;
import com.aboni.nmea.router.impl.RouterMessageFactoryImpl;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TalkerId;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class NMEAFilterSetImplTest {

    private static RouterMessage getRouterMessage(Sentence test) {
        return new RouterMessageFactoryImpl(new NMEA0183MessageFactoryImpl()).createMessage(test, "WHATEVER", System.currentTimeMillis());
    }

    @Test
    public void testEmptyWhiteList() {
        NMEAFilterSetImpl set = new NMEAFilterSetImpl(TYPE.WHITELIST);
        Sentence test = SentenceFactory.getInstance().createParser(TalkerId.II, "GLL");
        // any sentence fails
        assertFalse(set.match(getRouterMessage(test)));
    }

	@Test
	public void testWhiteList() {
        NMEABasicSentenceFilter f1 = new NMEABasicSentenceFilter("GLL");
        NMEABasicSentenceFilter f2 = new NMEABasicSentenceFilter("HDG");
        NMEAFilterSetImpl set = new NMEAFilterSetImpl(TYPE.WHITELIST);
        set.addFilter(f1);
        set.addFilter(f2);

        Sentence test = SentenceFactory.getInstance().createParser(TalkerId.II, "GLL");
        assertTrue(set.match(getRouterMessage(test)));

        test = SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
        assertTrue(set.match(getRouterMessage(test)));

        test = SentenceFactory.getInstance().createParser(TalkerId.II, "GGA");
        assertFalse(set.match(getRouterMessage(test)));
    }

    @Test
	public void testEmptyBlackList() {
        NMEAFilterSetImpl set = new NMEAFilterSetImpl(TYPE.BLACKLIST);
        Sentence test = SentenceFactory.getInstance().createParser(TalkerId.II, "GLL");
        assertTrue(set.match(getRouterMessage(test)));
    }
	
	@Test
	public void testBlackList() {
        NMEABasicSentenceFilter f1 = new NMEABasicSentenceFilter("GLL");
        NMEABasicSentenceFilter f2 = new NMEABasicSentenceFilter("HDG");
        NMEAFilterSetImpl set = new NMEAFilterSetImpl(TYPE.BLACKLIST);
        set.addFilter(f1);
        set.addFilter(f2);

        Sentence test = SentenceFactory.getInstance().createParser(TalkerId.II, "GLL");
        assertFalse(set.match(getRouterMessage(test)));

        test = SentenceFactory.getInstance().createParser(TalkerId.II, "HDG");
        assertFalse(set.match(getRouterMessage(test)));

        test = SentenceFactory.getInstance().createParser(TalkerId.II, "GGA");
        assertTrue(set.match(getRouterMessage(test)));
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
        assertTrue(set.match(getRouterMessage(test)));

        test = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A*0B");
        assertTrue(set.match(getRouterMessage(test)));

        test = SentenceFactory.getInstance().createParser("$STALK,23,01,0E,39*3E");
        assertFalse(set.match(getRouterMessage(test)));
    }

    @Test
    public void testToJSONWhite() {
        DummyFilter f = new DummyFilter("aa");
        NMEAFilterSetImpl set = new NMEAFilterSetImpl(TYPE.WHITELIST);
        set.addFilter(f);
        JSONObject jFSet = set.toJSON().getJSONObject("filter");
        System.out.println(jFSet);
        assertEquals("set", jFSet.getString("type"));
        assertEquals("whitelist", jFSet.getString("logic"));
        assertEquals(1, jFSet.getJSONArray("filters").length());
        JSONObject jF = jFSet.getJSONArray("filters").getJSONObject(0).getJSONObject("filter");
        assertEquals("dummy", jF.getString("type"));
        assertEquals("aa", jF.getString("data"));
    }

    @Test
    public void testParse() {
        JSONObject j = new JSONObject("{\"filter\": {\"logic\":\"whitelist\",\"filters\":[{\"filter\":{\"data\":\"aa\",\"type\":\"dummy\"}}],\"type\":\"set\"}}");
        NMEAFilterSet fSet = NMEAFilterSetImpl.parseFilter(j, DummyFilter::parseFilter);
        assertEquals(TYPE.WHITELIST, fSet.getType());
        assertFalse(fSet.isEmpty());
        List<NMEAFilter> list = new ArrayList<>();
        for (Iterator<NMEAFilter> i = fSet.getFilters(); i.hasNext(); ) list.add(i.next());
        assertEquals(1, list.size());
        assertEquals(DummyFilter.class, list.get(0).getClass());
        assertEquals("aa", ((DummyFilter)list.get(0)).getData());
    }
}
