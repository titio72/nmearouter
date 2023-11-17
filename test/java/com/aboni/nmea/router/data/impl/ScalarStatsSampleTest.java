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

import com.aboni.nmea.router.data.impl.ScalarStatsSample;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ScalarStatsSampleTest {

    @Test
    public void testEmpty() {
        ScalarStatsSample sample = new ScalarStatsSample("TW_");
        Assert.assertEquals(0, sample.getSamples());
        assertTrue(Double.isNaN(sample.getValue()));
        assertTrue(Double.isNaN(sample.getMaxValue()));
        assertTrue(Double.isNaN(sample.getMinValue()));
    }

    @Test
    public void testOneSample() {
        long t0 = System.currentTimeMillis();
        ScalarStatsSample sample = new ScalarStatsSample("TW_");
        sample.add(8.0, t0);

        Assert.assertEquals(1, sample.getSamples());
        Assert.assertEquals(8.0, sample.getValue(), 0.001);
        Assert.assertEquals(8.0, sample.getMinValue(), 0.001);
        Assert.assertEquals(8.0, sample.getMaxValue(), 0.001);
        Assert.assertEquals(t0, sample.getT0());
        Assert.assertEquals(t0, sample.getT1());
    }

    @Test
    public void testOneSampleMaxMin() {
        long t0 = System.currentTimeMillis();
        ScalarStatsSample sample = new ScalarStatsSample("TW_");
        sample.add(12.0, 8.0, 6.0, t0);

        Assert.assertEquals(1, sample.getSamples());
        Assert.assertEquals(8.0, sample.getValue(), 0.001);
        Assert.assertEquals(6.0, sample.getMinValue(), 0.001);
        Assert.assertEquals(12.0, sample.getMaxValue(), 0.001);
        Assert.assertEquals(t0, sample.getT0());
        Assert.assertEquals(t0, sample.getT1());
    }
    @Test
    public void testMultipleSamples() {
        long t0 = System.currentTimeMillis();
        ScalarStatsSample sample = new ScalarStatsSample("TW_");
        sample.add(11.0, t0);
        sample.add(12.0, t0 + 1000);
        sample.add(13.0, t0 + 2000);

        Assert.assertEquals(3, sample.getSamples());
        Assert.assertEquals(12, sample.getValue(), 0.001);
        Assert.assertEquals(11, sample.getMinValue(), 0.001);
        Assert.assertEquals(13, sample.getMaxValue(), 0.001);
        Assert.assertEquals(t0, sample.getT0());
        Assert.assertEquals(t0 + 2000, sample.getT1());
    }
}