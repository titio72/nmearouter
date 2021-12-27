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

package com.aboni.nmea.router.impl;

import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultTimestampProviderTest {

    @Test
    public void testGetTime() {
        long ts = System.currentTimeMillis();
        DefaultTimestampProvider t = new DefaultTimestampProvider();
        assertTrue((t.getNow() - ts) < 1);
    }

    @Test
    public void testSynced() {
        DefaultTimestampProvider t = new DefaultTimestampProvider();
        t.setSkew(System.currentTimeMillis(), 100);
        assertTrue(t.isSynced());
    }

    @Test
    public void testSkew() {
        DefaultTimestampProvider t = new DefaultTimestampProvider();
        t.setSkew(System.currentTimeMillis() + 55, 100);
        assertEquals(-55, t.getSkew(), 2); // the timestamp provider is 55ms late (account 2ms margin...)
    }

    @Test
    public void testNotSynced() {
        DefaultTimestampProvider t = new DefaultTimestampProvider();
        t.setSkew(System.currentTimeMillis() + 1000, 100); // skewed by 1s with a tolerance of 100ms
        assertFalse(t.isSynced());
    }
}