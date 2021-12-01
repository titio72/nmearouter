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

package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.message.Message;
import com.aboni.utils.ConsoleLog;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NMEAStreamImplTest {

    public static class MyObserver {

        private boolean err;

        public boolean isErr() {
            return err;
        }

        public void setErr(boolean err) {
            this.err = err;
        }

        List<RouterMessage> recv = new ArrayList<>();

        @OnRouterMessage
        public void onMessage(RouterMessage m) {
            recv.add(m);
        }
    }

    private static class MyMessage implements Message {

        @Override
        public String getMessageContentType() {
            return "test";
        }
    }

    @Test
    public void test1Obs() {
        RouterMessage mSend = new RouterMessageImpl<Message>(new MyMessage(), "TEST", 0);
        NMEAStreamImpl s = new NMEAStreamImpl(ConsoleLog.getLogger());
        MyObserver obs = new MyObserver();
        s.subscribe(obs);
        s.pushMessage(mSend);
        assertEquals(1, obs.recv.size());
        assertEquals(mSend, obs.recv.get(0));
    }

    @Test
    public void test2Obs() {
        RouterMessage mSend = new RouterMessageImpl<Message>(new MyMessage(), "TEST", 0);
        NMEAStreamImpl s = new NMEAStreamImpl(ConsoleLog.getLogger());
        MyObserver obs1 = new MyObserver();
        MyObserver obs2 = new MyObserver();
        s.subscribe(obs1);
        s.subscribe(obs2);
        assertEquals(2, s.getSubscribersCount());
        s.pushMessage(mSend);
        assertEquals(1, obs1.recv.size());
        assertEquals(mSend, obs1.recv.get(0));
        assertEquals(1, obs2.recv.size());
        assertEquals(mSend, obs2.recv.get(0));
    }

    @Test
    public void testDropErrObs() {
        RouterMessage mSend = new RouterMessageImpl<Message>(new MyMessage(), "TEST", 0);
        NMEAStreamImpl s = new NMEAStreamImpl(ConsoleLog.getLogger());
        MyObserver obs1 = new MyObserver();
        MyObserver obs2 = new MyObserver();
        s.subscribe(obs1);
        s.subscribe(obs2);
        obs1.setErr(true);
        s.pushMessage(mSend, observer -> !((MyObserver) observer).isErr());
        assertEquals(0, obs1.recv.size());
        assertEquals(1, obs2.recv.size());
        assertEquals(mSend, obs2.recv.get(0));
        assertEquals(1, s.getSubscribersCount());
    }
}