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

package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.impl.DefaultTimestampProvider;
import com.aboni.utils.ConsoleLog;
import com.aboni.utils.ThingsFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class NMEAAgentImplTest {

    private DefaultTimestampProvider tp = new DefaultTimestampProvider();

    @Before
    public void setup() {
        Injector injector = Guice.createInjector(new NMEARouterModule());
        ThingsFactory.setInjector(injector);
    }

    private class MyAgent extends NMEAAgentImpl {

        private final List<String> trail = new ArrayList<>();

        private boolean activateAnswer = true;

        public MyAgent(boolean source, boolean target) {
            super(ConsoleLog.getLogger(), tp, source, target);
        }

        void setOnActivateAnswer(boolean b) {
            activateAnswer = b;
        }

        @Override
        protected void onDeactivate() {
            trail.add("on deactivate");
        }

        @Override
        protected boolean onActivate() {
            trail.add("on activate");
            return activateAnswer;
        }
    }

    @Test
    public void testStart() {
        MyAgent a = new MyAgent(false, false);
        assertFalse(a.isStarted());
        a.setOnActivateAnswer(true);
        a.start();
        assertTrue(a.isStarted());
        assertEquals(1, a.trail.size());
        assertEquals("on activate", a.trail.get(0));
    }

    @Test
    public void testStartTwice() {
        MyAgent a = new MyAgent(false, false);
        assertFalse(a.isStarted());
        a.setOnActivateAnswer(true);
        a.start();
        a.trail.clear();

        a.start();
        assertTrue(a.isStarted());
        assertEquals(0, a.trail.size());
    }

    @Test
    public void testStartFail() {
        MyAgent a = new MyAgent(false, false);
        assertFalse(a.isStarted());
        a.setOnActivateAnswer(false);
        a.start();
        assertFalse(a.isStarted());
        assertEquals(1, a.trail.size());
        assertEquals("on activate", a.trail.get(0));
    }

    @Test
    public void testStop() {
        MyAgent a = new MyAgent(false, false);
        assertFalse(a.isStarted());
        a.setOnActivateAnswer(true);
        a.start();
        a.trail.clear();

        a.stop();
        assertFalse(a.isStarted());
        assertEquals(1, a.trail.size());
        assertEquals("on deactivate", a.trail.get(0));
    }


}