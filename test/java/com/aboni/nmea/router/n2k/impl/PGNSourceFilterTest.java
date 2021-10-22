/*
 * Copyright (c) 2021,  Andrea Boni
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

package com.aboni.nmea.router.n2k.impl;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PGNSourceFilterTest {

    @Test
    public void testSupportedNoSourceMatch() {
        PGNSourceFilterImpl f = new PGNSourceFilterImpl(null);
        f.loadSourceLine("65306,205");
        assertFalse(f.accept(99, 65306));
        assertFalse(f.accept(99, 65306, System.currentTimeMillis()));
    }

    @Test
    public void testSupportedSourceMatch() {
        PGNSourceFilterImpl f = new PGNSourceFilterImpl(null);
        f.loadSourceLine("65306,205");
        assertTrue(f.accept(205, 65306));
        assertTrue(f.accept(205, 65306, System.currentTimeMillis()));
    }

    @Test
    public void testNotSupported() {
        PGNSourceFilterImpl f = new PGNSourceFilterImpl(null);
        f.loadSourceLine("65306,205");
        assertFalse(f.accept(205, 65301));
        assertFalse(f.accept(205, 65301, System.currentTimeMillis()));
    }

    @Test
    public void testMatchPrimary() {
        PGNSourceFilterImpl f = new PGNSourceFilterImpl(null);
        f.loadSourceLine("65306,205,208");
        assertTrue(f.accept(205, 65306));
        assertTrue(f.accept(205, 65306, System.currentTimeMillis()));
    }

    @Test
    public void testMatchSecondary() {
        PGNSourceFilterImpl f = new PGNSourceFilterImpl(null);
        f.loadSourceLine("65306,205,208");
        assertFalse(f.accept(208, 65306)); // fails because it needs to check the elapsed time (so need a time)
        assertTrue(f.accept(208, 65306, System.currentTimeMillis()));
    }

    @Test
    public void testMatchSecondaryButTooSoon() {
        PGNSourceFilterImpl f = new PGNSourceFilterImpl(null);
        f.loadSourceLine("65306,205,208");
        f.setPGNTimestamp(205, 65306, 1000 /* arbitrary time */);
        assertFalse(f.accept(208, 65306, 2000)); // just 1 seconds passed
    }

    @Test
    public void testMatchSecondaryAfterPrimaryTimeout() {
        PGNSourceFilterImpl f = new PGNSourceFilterImpl(null);
        f.loadSourceLine("65306,205,208");
        f.setPGNTimestamp(205, 65306, 1000 /* arbitrary time */);
        assertTrue(f.accept(208, 65306, 12000)); // 11 seconds elapsed since primary (>10s)
    }

}