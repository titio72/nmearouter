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

/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.data;

public abstract class StatsSample implements Sample {

    private final String tag;
    protected double avg;
    protected double max;
    protected double min;
    protected long t0;
    protected long t1;
    protected int samples = 0;

    @Override
    public Sample getImmutableCopy() {
        return ImmutableSample.newInstance(getT0(), getTag(), getMinValue(), getValue(), getMaxValue());
    }

    @Override
    public String getTag() {
        return tag;
    }

    public int getSamples() {
        return samples;
    }

    @Override
    public double getValue() {
        return avg;
    }

    @Override
    public double getMinValue() {
        return min;
    }

    @Override
    public double getMaxValue() {
        return max;
    }

    @Override
    public long getTimestamp() {
        return getT0();
    }

    public long getT0() {
        return t0;
    }

    public long getT1() {
        return t1;
    }

    protected StatsSample(String tag) {
        this.tag = tag;
        reset();
    }

    public long getInterval() {
        return t1 - t0;
    }

    public void reset() {
        avg = Double.NaN;
        max = Double.NaN;
        min = Double.NaN;
        t0 = 0;
        t1 = 1;
        samples = 0;
    }

    public abstract void add(double value, long time);

    public abstract void add(double max, double value, double min, long time);

    public abstract StatsSample cloneStats();
}
