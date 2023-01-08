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

public class ImmutableSample implements Sample {

    private final long ts;
    private final String tag;
    private final double maxValue;
    private final double minValue;
    private final double value;

    public static Sample newInstance(long time, String tag, double min, double v, double max) {
        return new ImmutableSample(time, tag, min, v, max);
    }

    @Override
    public Sample getImmutableCopy() {
        return newInstance(getTimestamp(), getTag(), getMinValue(), getValue(), getMaxValue());
    }

    protected ImmutableSample(long time, String tag, double min, double v, double max) {
        this.ts = time;
        this.tag = tag;
        this.minValue = min;
        this.value = v;
        this.maxValue = max;
    }

    @Override
    public long getTimestamp() {
        return ts;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public double getMaxValue() {
        return maxValue;
    }

    @Override
    public double getMinValue() {
        return minValue;
    }

    @Override
    public double getValue() {
        return value;
    }
}
