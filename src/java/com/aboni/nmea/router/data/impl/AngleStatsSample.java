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
import com.aboni.utils.Utils;

public class AngleStatsSample extends StatsSample {

    @Override
    public StatsSample cloneStats() {
        AngleStatsSample clone = new AngleStatsSample(getTag());
        clone.avg = avg;
        clone.max = max;
        clone.min = min;
        clone.t0 = t0;
        clone.t1 = t1;
        clone.samples = samples;
        return clone;
    }

    public AngleStatsSample(String tag) {
        super(tag);
    }


    @Override
    public void add(double vMin, double v, double vMax, long time) {
        // ignore max & min input
        add(v, time);
    }

    @Override
    public void add(double v, long time) {
        if (samples == 0) {
            t0 = time;
            t1 = time;
            v = Utils.normalizeDegrees0To360(v);
            avg = v;
            max = v;
            min = v;
            samples = 1;
        } else {
            t1 = time;
            double a = Utils.getNormal180(avg, v);
            avg = ((avg * samples) + a) / (samples + 1);
            avg = Utils.normalizeDegrees0To360(avg);
            max = Utils.normalizeDegrees0To360(Math.max(max, a));
            min = Utils.normalizeDegrees0To360(Math.min(min, a));
            samples++;
        }
    }
}
