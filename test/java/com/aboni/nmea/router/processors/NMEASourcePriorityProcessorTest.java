package com.aboni.nmea.router.processors;

import com.aboni.nmea.message.Message;
import com.aboni.nmea.nmea0183.NMEA0183MessageFactory;
import com.aboni.nmea.nmea0183.impl.NMEA0183MessageFactoryImpl;
import com.aboni.nmea.router.processors.NMEASourcePriorityProcessor;
import com.aboni.utils.ProgrammableTimeStampProvider;
import com.aboni.data.Pair;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NMEASourcePriorityProcessorTest {

    private NMEASourcePriorityProcessor proc;
    private ProgrammableTimeStampProvider timestampProvider;

    private NMEA0183MessageFactory msgFactory = new NMEA0183MessageFactoryImpl();

    @Before
    public void setUp() {
        timestampProvider = new ProgrammableTimeStampProvider();
        proc = new NMEASourcePriorityProcessor(timestampProvider);
    }

    @Test
    public void testProcessEmpty() {
        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A*0B");
        Pair<Boolean, Message[]> res = proc.process(msgFactory.getMessage(s), "MySrc");
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
        Pair<Boolean, Message[]> res = proc.process(msgFactory.getMessage(s), "MySrc");
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
        Pair<Boolean, Message[]> res = proc.process(msgFactory.getMessage(s), "MySrc");
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
        Pair<Boolean, Message[]> res = proc.process(msgFactory.getMessage(s), "MySrc");
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
        proc.process(msgFactory.getMessage(s0), "MySrc");

        Sentence s2 = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A");
        Pair<Boolean, Message[]> res = proc.process(msgFactory.getMessage(s2), "MySrc");
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
        proc.process(msgFactory.getMessage(s0), "MySrc");

        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A");
        Pair<Boolean, Message[]> res = proc.process(msgFactory.getMessage(s), "MyOtherSrc");
        assertNotNull(res);
        assertFalse(res.first);
    }

    @Test
    public void testAllowLowPriorityAfterTimeout() {
        proc.setSentences(new String[]{"MWV", "VWR"});
        proc.setPriority("MySrc", 10);
        proc.setPriority("MyOtherSrc", 5);

        long t0 = 10000000L;
        timestampProvider.setTimestamp(t0);
        Sentence s0 = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A");
        proc.process(msgFactory.getMessage(s0), "MySrc");

        timestampProvider.setTimestamp(t0 + /* 3 minutes : 1 more than the threshold */ 3L * 60000L);
        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A");
        Pair<Boolean, Message[]> res = proc.process(msgFactory.getMessage(s), "MyOtherSrc");
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
        timestampProvider.setTimestamp(t0);
        Sentence s0 = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A");
        proc.process(msgFactory.getMessage(s0), "MySrc");

        timestampProvider.setTimestamp(t0 + /* 3 minutes : 1 more than the threshold */ 3L * 60000L);
        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A");
        Pair<Boolean, Message[]> res = proc.process(msgFactory.getMessage(s), "MySrc");
        assertNotNull(res);
        assertTrue(res.first);
        assertNotNull(res.second);
        assertEquals(0, res.second.length);
    }

}