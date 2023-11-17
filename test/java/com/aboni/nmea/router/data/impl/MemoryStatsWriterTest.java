/*
 * Copyright (c) 2022,  Andrea Boni
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

import com.aboni.nmea.router.data.impl.MemoryStatsWriter;
import com.aboni.nmea.router.data.impl.ScalarStatsSample;
import com.aboni.nmea.router.data.metrics.Metric;
import com.aboni.nmea.router.data.metrics.Metrics;
import org.junit.Test;

import static org.junit.Assert.*;

public class MemoryStatsWriterTest {


    @Test
    public void testInit() {
        MemoryStatsWriter writer = new MemoryStatsWriter();
        writer.init();
        // just do not crash
        assertTrue(true);
    }

    @Test
    public void testDispose() {
        MemoryStatsWriter writer = new MemoryStatsWriter();
        writer.init();
        writer.dispose();
        // just do not crash
        assertTrue(true);
    }

    private static void loadSample(MemoryStatsWriter writer, Metric metric, double value, long timestamp) {
        ScalarStatsSample s = new ScalarStatsSample(metric.getId());
        s.add(value, timestamp);
        writer.write(s.getImmutableCopy(), timestamp);
    }

    @Test
    public void testAddSampleAndGetHistory() {
        MemoryStatsWriter writer = new MemoryStatsWriter();
        writer.init();
        long now = System.currentTimeMillis();
        loadSample(writer, Metrics.PRESSURE, 1025, now - 1000);
        loadSample(writer, Metrics.PRESSURE, 1026, now);
        assertNotNull(writer.getHistory(Metrics.PRESSURE));
        assertEquals(2, writer.getHistory(Metrics.PRESSURE).size());
    }
}