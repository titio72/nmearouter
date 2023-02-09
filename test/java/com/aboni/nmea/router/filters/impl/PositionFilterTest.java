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

package com.aboni.nmea.router.filters.impl;

import com.aboni.nmea.router.utils.PositionGenerator;
import com.aboni.utils.Pair;
import net.sf.marineapi.nmea.util.Position;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PositionFilterTest {

    private PositionFilter filter;
    private Position startPos;

    @Before
    public void setup() {
        filter = new PositionFilter(30);
        startPos = new Position(43.9599, 09.7745);
    }

    private Pair<Position, Long> goReady() {
        return goReady(0);
    }

    private Pair<Position, Long> goReady(long t0) {
        int i = 0;
        Position p = null;
        while (!filter.isReady() && i < 1000 /* safeguard */) {
            // move west at 5.4Kn for 1 second
            p = PositionGenerator.getNewPos(startPos, 270, i * (5.4 / 3600.0));
            long t = t0 + i * 1000;
            filter.acceptPoint(PositionGenerator.getMessage(p, 90, 5.4, t));
            i++;
        }
        return new Pair<>(p, t0 + ((i - 1) * 1000));
    }

    @Test
    public void testGoReady() {
        Pair<Position, Long> ready = goReady();
        assertEquals(29000L, ready.second.longValue());
        assertNotNull(ready.first);
    }

    @Test
    public void testExcludeSpike() {
        Pair<Position, Long> ready = goReady();
        // jump north by 0.33 miles (too much for one second sample)
        Position p = PositionGenerator.getNewPos(ready.first, 0, PositionFilter.getMaxIncrementalDistance() * 1.1);
        assertFalse(filter.acceptPoint(PositionGenerator.getMessage(p, 0, 10, ready.second + 1000)));
    }

    @Test
    public void testExcludeSpeedSpike() {
        Pair<Position, Long> ready = goReady();
        Position p = PositionGenerator.getNewPos(ready.first, 270, 0.0015);
        // distance is ok but reported SOG is too high
        assertFalse(filter.acceptPoint(PositionGenerator.getMessage(p, 0, PositionFilter.getMaxAllowedSpeed() * 2.0, ready.second + 1000)));
    }

    @Test
    public void testResumeAfterSpike() {
        Pair<Position, Long> ready = goReady();
        // jump north by 0.33 miles (too much for one second sample - max is 0.3)
        Position p = PositionGenerator.getNewPos(ready.first, 0, PositionFilter.getMaxIncrementalDistance() * 1.1);
        assertFalse(filter.acceptPoint(PositionGenerator.getMessage(p, 0, 10, ready.second + 1000)));
        // resume moving west at 5.4Kn on the ideal course
        p = PositionGenerator.getNewPos(ready.first, 270, (2.0 * 0.0015));
        assertTrue(filter.acceptPoint(PositionGenerator.getMessage(p, 0, 5.4, ready.second + 2000)));
    }

    @Test
    public void testResetAfterLongInactivity() {
        // verify that it becomes not-ready after 5 minutes of inactivity
        Pair<Position, Long> ready = goReady();
        // jump west by 2 miles and 20m simulating a missing GPS signal for 20 minutes
        Position p = PositionGenerator.getNewPos(ready.first, 270, 2);
        assertFalse(filter.acceptPoint(PositionGenerator.getMessage(p, 0, 10, ready.second + (1000 * 60 * 20))));
        assertFalse(filter.isReady());
    }

    @Test
    public void testResetAfterManyInvalidMessages() {
        // verify that it becomes not-ready after 5 minutes of invalid messages
        Pair<Position, Long> ready = goReady();
        // 20m of invalid messages
        for (int i = 0; i < (60 * 20); i++) {
            filter.acceptPoint(PositionGenerator.getMessage(null, 270, 5.4, ready.second + (1000 * i)));
        }
        assertFalse(filter.isReady());
    }

    @Test
    public void testSkipReversedTime() {
        Pair<Position, Long> ready = goReady();
        assertFalse(filter.acceptPoint(PositionGenerator.getMessage(null, 270, 5.4, ready.second - 1000)));
    }

    @Test
    public void testSkipInvalid() {
        Pair<Position, Long> ready = goReady();
        assertFalse(filter.acceptPoint(PositionGenerator.getMessage(null, 270, 5.4, ready.second + 1000)));
    }

    @Test
    public void testResumeAfterReset() {
        // go ready
        Pair<Position, Long> ready = goReady();
        // now reset
        Position p = PositionGenerator.getNewPos(ready.first, 270, 2);
        assertFalse(filter.acceptPoint(PositionGenerator.getMessage(p, 0, 10, ready.second + (1000 * 60 * 20))));
        assertFalse(filter.isReady());
        // then resume
        goReady(ready.second + 1000 * 60 * 20 + 1000);
        assertTrue(filter.isReady());
    }
}