package com.aboni.nmea.router.processors;

import com.aboni.nmea.router.message.Message;
import com.aboni.utils.Pair;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;




public class NMEAProcessorSetTest {

    private NMEAProcessorSet theSet;

    private static class MyProc implements NMEAPostProcess {

        private Pair<Boolean, Message[]> nextAnswer;

        @Override
        public Pair<Boolean, Message[]> process(Message message, String src) {
            Pair<Boolean, Message[]> res = nextAnswer;
            nextAnswer = null;
            return res;
        }

        @Override
        public void onTimer() {

        }

        void setNextAnswer(boolean accept, Message s) {
            setNextAnswer(accept, new Message[] {s});
        }

        void setNextAnswer(boolean accept, Message[] s) {
            nextAnswer = new Pair<>(accept, s);
        }
    }

    private static class MyMessage implements Message {

    }

    @Before
    public void setUp() {
        theSet = new NMEAProcessorSet();
    }

    @Test
    public void testEmptyProcSet() throws NMEARouterProcessorException {
        Message s = new MyMessage();
        List<Message> res = theSet.getSentences(s, "MySrc");

        // check accepted
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(s, res.get(0));
    }

    @Test
    public void testSimpleProc() throws NMEARouterProcessorException {
        theSet.addProcessor(new MyProc());

        Message s = new MyMessage();
        List<Message> res = theSet.getSentences(s, "MySrc");

        // check accepted
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(s, res.get(0));
    }

    @Test
    public void testSimpleProc1() throws NMEARouterProcessorException {
        MyProc p = new MyProc();
        theSet.addProcessor(p);

        Message s = new MyMessage();

        p.setNextAnswer(true, new Message[]{});
        List<Message> res = theSet.getSentences(s, "MySrc");

        // check accepted
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(s, res.get(0));
    }

    @Test
    public void testSimpleProcDrop() throws NMEARouterProcessorException {
        MyProc p = new MyProc();
        theSet.addProcessor(p);


        Message s = new MyMessage();

        p.setNextAnswer(false, s);
        List<Message> res = theSet.getSentences(s, "MySrc");

        // check accepted
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    public void testSimpleProcAdd() throws NMEARouterProcessorException {
        MyProc p = new MyProc();
        theSet.addProcessor(p);


        Message s = new MyMessage();

        Message s1 = new MyMessage();

        p.setNextAnswer(true, s1);
        List<Message> res = theSet.getSentences(s, "MySrc");

        // check accepted
        assertNotNull(res);
        assertEquals(2, res.size());
        assertEquals(s, res.get(0));
        assertEquals(s1, res.get(1));

    }

    @Test
    public void testTwoSimpleProcsAccept() throws NMEARouterProcessorException {
        theSet.addProcessor(new MyProc());
        theSet.addProcessor(new MyProc());

        Message s = new MyMessage();
        List<Message> res = theSet.getSentences(s, "MySrc");

        // check accepted
        assertNotNull(res);
        assertEquals(1, res.size());
        assertEquals(s, res.get(0));
    }

    @Test
    public void testTwoSimpleProcsDrop() throws NMEARouterProcessorException {
        MyProc p = new MyProc();
        theSet.addProcessor(new MyProc());
        theSet.addProcessor(p);

        Message s = new MyMessage();
        p.setNextAnswer(false, s);
        List<Message> res = theSet.getSentences(s, "MySrc");
        // check accepted
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }




}