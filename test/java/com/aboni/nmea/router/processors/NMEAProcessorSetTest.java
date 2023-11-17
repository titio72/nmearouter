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

import com.aboni.nmea.message.Message;
import com.aboni.data.Pair;
import com.aboni.nmea.router.processors.NMEAPostProcess;
import com.aboni.nmea.router.processors.NMEAProcessorSet;
import com.aboni.nmea.router.processors.NMEARouterProcessorException;
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

        @Override
        public String getMessageContentType() {
            return "";
        }
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