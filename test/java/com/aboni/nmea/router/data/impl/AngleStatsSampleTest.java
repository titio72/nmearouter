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

package com.aboni.nmea.router.data.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AngleStatsSampleTest {

    @Test
    public void testEmpty() {
        AngleStatsSample sample = new AngleStatsSample("TWD");
        assertEquals(0, sample.getSamples());
        assertTrue(Double.isNaN(sample.getAvg()));
        assertTrue(Double.isNaN(sample.getMax()));
        assertTrue(Double.isNaN(sample.getMin()));
    }

    @Test
    public void testOneSample() {
        long t0 = System.currentTimeMillis();
        AngleStatsSample sample = new AngleStatsSample("TWD");
        sample.add(45 /* degrees */, t0);

        assertEquals(1, sample.getSamples());
        assertEquals(45, sample.getAvg(), 0.001);
        assertEquals(45, sample.getMin(), 0.001);
        assertEquals(45, sample.getMax(), 0.001);
        assertEquals(t0, sample.getT0());
        assertEquals(t0, sample.getT1());
    }

    @Test
    public void testMultipleSamples() {
        long t0 = System.currentTimeMillis();
        AngleStatsSample sample = new AngleStatsSample("TWD");
        sample.add(45 /* degrees */, t0);
        sample.add(44 /* degrees */, t0 + 1000);
        sample.add(46 /* degrees */, t0 + 2000);

        assertEquals(3, sample.getSamples());
        assertEquals(45, sample.getAvg(), 0.001);
        assertEquals(44, sample.getMin(), 0.001);
        assertEquals(46, sample.getMax(), 0.001);
        assertEquals(t0, sample.getT0());
        assertEquals(t0 + 2000, sample.getT1());
    }

    @Test
    public void testMultipleSamplesCross360() {
        long t0 = System.currentTimeMillis();
        AngleStatsSample sample = new AngleStatsSample("TWD");
        sample.add(350 /* degrees */, t0);
        sample.add(5 /* degrees */, t0 + 1000);
        sample.add(20 /* degrees */, t0 + 2000);

        assertEquals(3, sample.getSamples());
        assertEquals(5, sample.getAvg(), 0.001);
        assertEquals(350, sample.getMin(), 0.001);
        assertEquals(20, sample.getMax(), 0.001);
        assertEquals(t0, sample.getT0());
        assertEquals(t0 + 2000, sample.getT1());
    }

    @Test
    public void testMultipleSamplesCross360_2() {
        long t0 = System.currentTimeMillis();
        AngleStatsSample sample = new AngleStatsSample("TWD");
        sample.add(350 /* degrees */, t0);
        sample.add(358 /* degrees */, t0 + 1000);
        sample.add(6 /* degrees */, t0 + 2000);

        assertEquals(3, sample.getSamples());
        assertEquals(358, sample.getAvg(), 0.001);
        assertEquals(350, sample.getMin(), 0.001);
        assertEquals(6, sample.getMax(), 0.001);
        assertEquals(t0, sample.getT0());
        assertEquals(t0 + 2000, sample.getT1());
    }


}