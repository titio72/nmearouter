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

package com.aboni.nmea.router.data.impl;

import com.aboni.nmea.router.data.StatsSample;

public class ScalarStatsSample extends StatsSample {

    private final double rangeMin;
    private final double rangeMax;

    @Override
    public StatsSample cloneStats() {
        ScalarStatsSample clone = new ScalarStatsSample(getTag(), getRangeMin(), getRangeMax());
        clone.avg = avg;
        clone.max = max;
        clone.min = min;
        clone.samples = samples;
        clone.t0 = t0;
        clone.t1 = t1;
        return clone;
    }

    public ScalarStatsSample(String tag, double min, double max) {
        super(tag);
        this.rangeMax = max;
        this.rangeMin = min;
    }

    public ScalarStatsSample(String tag) {
        this(tag, Double.NaN, Double.NaN);
    }

    public double getRangeMin() {
        return rangeMin;
    }

    public double getRangeMax() {
        return rangeMax;
    }

    @Override
    public void add(double vMin, double v, double vMax, long time) {
        if (isInRange(v)) {
            if (samples == 0) {
                initSample(vMin, v, vMax, time);
            } else {
                updateSample(vMin, v, vMax, time);
            }
        }
    }

    private void updateSample(double vMin, double v, double vMax, long time) {
        avg = (avg * samples + v) / (samples + 1);
        if (isInRange(vMin)) min = Double.isNaN(min) ? vMin : Math.min(min, vMin);
        if (isInRange(vMax)) max = Double.isNaN(max) ? vMax : Math.max(max, vMax);
        t1 = time;
        samples++;
    }

    private void initSample(double vMin, double v, double vMax, long time) {
        t0 = time;
        t1 = time;
        avg = v;
        if (isInRange(vMax)) max = vMax;
        else max = v;
        if (isInRange(vMin)) min = vMin;
        else min = v;
        samples = 1;
    }

    private boolean isInRange(double v) {
        return !Double.isNaN(v) &&
                (Double.isNaN(getRangeMin()) || v >= getRangeMin()) &&
                (Double.isNaN(getRangeMax()) || v <= getRangeMax());
    }

    @Override
    public void add(double value, long time) {
        add(value, value, value, time);
    }
}
