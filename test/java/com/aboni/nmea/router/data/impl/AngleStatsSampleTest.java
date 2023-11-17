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

import com.aboni.nmea.router.data.impl.AngleStatsSample;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AngleStatsSampleTest {

    @Test
    public void testEmpty() {
        AngleStatsSample sample = new AngleStatsSample("TWD");
        Assert.assertEquals(0, sample.getSamples());
        assertTrue(Double.isNaN(sample.getValue()));
        assertTrue(Double.isNaN(sample.getMaxValue()));
        assertTrue(Double.isNaN(sample.getMinValue()));
    }

    @Test
    public void testOneSample() {
        long t0 = System.currentTimeMillis();
        AngleStatsSample sample = new AngleStatsSample("TWD");
        sample.add(45 /* degrees */, t0);

        Assert.assertEquals(1, sample.getSamples());
        Assert.assertEquals(45, sample.getValue(), 0.001);
        Assert.assertEquals(45, sample.getMinValue(), 0.001);
        Assert.assertEquals(45, sample.getMaxValue(), 0.001);
        Assert.assertEquals(t0, sample.getT0());
        Assert.assertEquals(t0, sample.getT1());
    }

    @Test
    public void testMultipleSamples() {
        long t0 = System.currentTimeMillis();
        AngleStatsSample sample = new AngleStatsSample("TWD");
        sample.add(45 /* degrees */, t0);
        sample.add(44 /* degrees */, t0 + 1000);
        sample.add(46 /* degrees */, t0 + 2000);

        Assert.assertEquals(3, sample.getSamples());
        Assert.assertEquals(45, sample.getValue(), 0.001);
        Assert.assertEquals(44, sample.getMinValue(), 0.001);
        Assert.assertEquals(46, sample.getMaxValue(), 0.001);
        Assert.assertEquals(t0, sample.getT0());
        Assert.assertEquals(t0 + 2000, sample.getT1());
    }

    @Test
    public void testMultipleSamplesCross360() {
        long t0 = System.currentTimeMillis();
        AngleStatsSample sample = new AngleStatsSample("TWD");
        sample.add(350 /* degrees */, t0);
        sample.add(5 /* degrees */, t0 + 1000);
        sample.add(20 /* degrees */, t0 + 2000);

        Assert.assertEquals(3, sample.getSamples());
        Assert.assertEquals(5, sample.getValue(), 0.001);
        Assert.assertEquals(350, sample.getMinValue(), 0.001);
        Assert.assertEquals(20, sample.getMaxValue(), 0.001);
        Assert.assertEquals(t0, sample.getT0());
        Assert.assertEquals(t0 + 2000, sample.getT1());
    }

    @Test
    public void testMultipleSamplesCross360_2() {
        long t0 = System.currentTimeMillis();
        AngleStatsSample sample = new AngleStatsSample("TWD");
        sample.add(350 /* degrees */, t0);
        sample.add(358 /* degrees */, t0 + 1000);
        sample.add(6 /* degrees */, t0 + 2000);

        Assert.assertEquals(3, sample.getSamples());
        Assert.assertEquals(358, sample.getValue(), 0.001);
        Assert.assertEquals(350, sample.getMinValue(), 0.001);
        Assert.assertEquals(6, sample.getMaxValue(), 0.001);
        Assert.assertEquals(t0, sample.getT0());
        Assert.assertEquals(t0 + 2000, sample.getT1());
    }


}