package com.aboni.nmea.router.processors;

import com.aboni.nmea.router.NMEACache;
import com.aboni.utils.DataEvent;
import com.aboni.utils.Pair;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.HeadingSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NMEASourcePriorityProcessorTest {

    private NMEASourcePriorityProcessor proc;
    private MyCache cache;

    private class MyCache implements NMEACache {

        @Override
        public DataEvent<HeadingSentence> getLastHeading() {
            return null;
        }

        @Override
        public DataEvent<PositionSentence> getLastPosition() {
            return null;
        }

        @Override
        public boolean isHeadingOlderThan(long time, long threshold) {
            return false;
        }

        @Override
        public void onSentence(Sentence s, String src) {

        }

        @Override
        public <T> void setStatus(String statusKey, T status) {

        }

        @Override
        public <T> T getStatus(String statusKey, T defaultValue) {
            return null;
        }

        @Override
        public long getNow() {
            return timestamp == -1 ? System.currentTimeMillis() : timestamp;
        }

        long timestamp = -1;
    }

    @Before
    public void setUp() {
        cache = new MyCache();
        proc = new NMEASourcePriorityProcessor(cache);
    }

    @Test
    public void testProcessEmpty() {
        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A*0B");
        Pair<Boolean, Sentence[]> res = proc.process(s, "MySrc");
        assertNotNull(res);
        assertTrue(res.first);
        assertNotNull(res.second);
        assertEquals(0, res.second.length);
    }

    @Test
    public void testProcessSentenceNotRelevant() {
        proc.setSentences(new String[] {"RMC", "GLL"});
        proc.setPriority("OtherSrc", 10);

        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A*0B");
        Pair<Boolean, Sentence[]> res = proc.process(s, "MySrc");
        assertNotNull(res);
        assertTrue(res.first);
        assertNotNull(res.second);
        assertEquals(0, res.second.length);
    }

    @Test
    public void testProcessSentenceRelevantFromOtherSource() {
        proc.setSentences(new String[] {"MWV", "VWR"});
        proc.setPriority("OtherSrc", 10);

        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A*0B");
        Pair<Boolean, Sentence[]> res = proc.process(s, "MySrc");
        assertNotNull(res);
        assertTrue(res.first);
        assertNotNull(res.second);
        assertEquals(0, res.second.length);
    }

    @Test
    public void testProcessSentenceRelevant() {
        proc.setSentences(new String[] {"MWV", "VWR"});
        proc.setPriority("MySrc", 10);

        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A*0B");
        Pair<Boolean, Sentence[]> res = proc.process(s, "MySrc");
        assertNotNull(res);
        assertTrue(res.first);
        assertNotNull(res.second);
        assertEquals(0, res.second.length);
    }

    @Test
    public void testProcess2SentenceRelevant() {
        proc.setSentences(new String[] {"MWV", "VWR"});
        proc.setPriority("MySrc", 10);

        Sentence s0 = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A");
        proc.process(s0, "MySrc");

        Sentence s2 = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A");
        Pair<Boolean, Sentence[]> res = proc.process(s2, "MySrc");
        assertNotNull(res);
        assertTrue(res.first);
        assertNotNull(res.second);
        assertEquals(0, res.second.length);
    }

    @Test
    public void testBlockSentence() {
        proc.setSentences(new String[] {"MWV", "VWR"});
        proc.setPriority("MySrc", 10);
        proc.setPriority("MyOtherSrc", 5);

        Sentence s0 = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A");
        proc.process(s0, "MySrc");

        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A");
        Pair<Boolean, Sentence[]> res = proc.process(s, "MyOtherSrc");
        assertNotNull(res);
        assertFalse(res.first);
    }

    @Test
    public void testAllowLowPriorityAfterTimeout() {
        proc.setSentences(new String[]{"MWV", "VWR"});
        proc.setPriority("MySrc", 10);
        proc.setPriority("MyOtherSrc", 5);

        long t0 = 10000000L;
        cache.timestamp = t0;
        Sentence s0 = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A");
        proc.process(s0, "MySrc");

        cache.timestamp = t0 + /* 3 minutes : 1 more than the threshold */ 3L * 60000L;
        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A");
        Pair<Boolean, Sentence[]> res = proc.process(s, "MyOtherSrc");
        assertNotNull(res);
        assertTrue(res.first);
        assertNotNull(res.second);
        assertEquals(0, res.second.length);
    }

    @Test
    public void testAllowHighPriorityAfterTimeou() {
        proc.setSentences(new String[] {"MWV", "VWR"});
        proc.setPriority("MySrc", 10);
        proc.setPriority("MyOtherSrc", 5);

        long t0 = 10000000L;
        cache.timestamp = t0;
        Sentence s0 = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A");
        proc.process(s0, "MySrc");

        cache.timestamp = t0 + /* 3 minutes : 1 more than the threshold */ 3L * 60000L;
        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A");
        Pair<Boolean, Sentence[]> res = proc.process(s, "MySrc");
        assertNotNull(res);
        assertTrue(res.first);
        assertNotNull(res.second);
        assertEquals(0, res.second.length);
    }

}