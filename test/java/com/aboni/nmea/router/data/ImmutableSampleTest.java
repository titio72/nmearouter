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

package com.aboni.nmea.router.data;

import com.aboni.nmea.router.data.ImmutableSample;
import com.aboni.nmea.router.data.Sample;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ImmutableSampleTest {

    @Test
    public void newInstance() {
        long now = System.currentTimeMillis();
        Sample s = ImmutableSample.newInstance(now, "TestTag", 0.1, 0.5, 0.9);
        assertEquals(0.1, s.getMinValue(), 0.00001);
        assertEquals(0.5, s.getValue(), 0.00001);
        assertEquals(0.9, s.getMaxValue(), 0.00001);
        assertEquals("TestTag", s.getTag());
        assertEquals(now, s.getTimestamp());
    }
}