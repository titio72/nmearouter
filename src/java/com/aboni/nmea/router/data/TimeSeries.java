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

import com.aboni.nmea.router.data.impl.ScalarStatsSample;

import java.util.ArrayList;
import java.util.List;

public class TimeSeries {

    private final List<Sample> samples;

    private StatsSample currentSample;
    private final long samplingPeriod;
    private final String tag;

    public TimeSeries(String tag, long samplingPeriod, int initialCapacity) {
        this.samplingPeriod = samplingPeriod;
        this.tag = tag;
        samples = new ArrayList<>(initialCapacity);
    }

    public void doSampling(long time, double vMax, double v, double vMin) {
        synchronized (this) {
            if (currentSample == null) {
                currentSample = new ScalarStatsSample(tag);
            }
            if (currentSample.getT0() > 0 && (time - currentSample.getT0()) > samplingPeriod) {
                samples.add(currentSample.getImmutableCopy());
                currentSample = new ScalarStatsSample(tag);
            }
            currentSample.add(vMax, v, vMin, time);
        }
    }

    public List<Sample> getSamples() {
        ArrayList<Sample> res;
        synchronized (this) {
            res = new ArrayList<>(samples);
            samples.add(currentSample.getImmutableCopy());
        }
        return res;
    }
}
