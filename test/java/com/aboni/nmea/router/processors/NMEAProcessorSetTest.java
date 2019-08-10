package com.aboni.nmea.router.processors;

import com.aboni.utils.Pair;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;




public class NMEAProcessorSetTest {

    private NMEAProcessorSet theSet;

    private static class MyProc implements NMEAPostProcess {

        private Pair<Boolean, Sentence[]> nextAnswer;

        @Override
        public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
            Pair<Boolean, Sentence[]> res = nextAnswer;
            nextAnswer = null;
            return res;
        }

        @Override
        public void onTimer() {

        }

        void setNextAnswer(boolean accept, Sentence s) {
            setNextAnswer(accept, new Sentence[] {s});
        }

        void setNextAnswer(boolean accept, Sentence[] s) {
            nextAnswer = new Pair<>(accept, s);
        }
    }

    @Before
    public void setUp() {
        theSet = new NMEAProcessorSet();
    }

    @Test
    public void testEmptyProcSet() {
        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A*0B");
        List<Sentence> res = theSet.getSentences(s, "MySrc");

        // check accepted
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(s, res.get(0));
    }

    @Test
    public void testSimpleProc() {
        theSet.addProcessor(new MyProc());

        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A*0B");
        List<Sentence> res = theSet.getSentences(s, "MySrc");

        // check accepted
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(s, res.get(0));
    }

    @Test
    public void testSimpleProc1() {
        MyProc p = new MyProc();
        theSet.addProcessor(p);

        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A*0B");

        p.setNextAnswer(true, new Sentence[] {});
        List<Sentence> res = theSet.getSentences(s, "MySrc");

        // check accepted
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(s, res.get(0));
    }

    @Test
    public void testSimpleProcDrop() {
        MyProc p = new MyProc();
        theSet.addProcessor(p);


        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,T,10.7,N,A*0B");

        p.setNextAnswer(false, s);
        List<Sentence> res = theSet.getSentences(s, "MySrc");

        // check accepted
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    public void testSimpleProcAdd() {
        MyProc p = new MyProc();
        theSet.addProcessor(p);


        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,R,10.7,N,A");

        Sentence s1 = SentenceFactory.getInstance().createParser("$IIMWV,95.2,T,7.7,N,A");

        p.setNextAnswer(true, s1);
        List<Sentence> res = theSet.getSentences(s, "MySrc");

        // check accepted
        assertNotNull(res);
        assertEquals(2, res.size());
        assertEquals(s, res.get(0));
        assertEquals(s1, res.get(1));

    }

    @Test
    public void testTwoSimpleProcsAccept() {
        theSet.addProcessor(new MyProc());
        theSet.addProcessor(new MyProc());

        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,R,10.7,N,A");
        List<Sentence> res = theSet.getSentences(s, "MySrc");

        // check accepted
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(s, res.get(0));
    }

    @Test
    public void testTwoSimpleProcsDrop() {
        MyProc p = new MyProc();
        theSet.addProcessor(new MyProc());
        theSet.addProcessor(p);

        Sentence s = SentenceFactory.getInstance().createParser("$IIMWV,102.5,R,10.7,N,A");
        p.setNextAnswer(false, s);
        List<Sentence> res = theSet.getSentences(s, "MySrc");
        // check accepted
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }




}