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

package com.aboni.utils;

import java.util.ArrayList;
import java.util.List;

public class TimeSeries {

    private final List<TimeSeriesSample> samples;
    private final long samplingPeriod;

    public TimeSeries(long samplingPeriod, int initialCapacity) {
        this.samplingPeriod = samplingPeriod;
        samples = new ArrayList<>(initialCapacity);
    }

    public void doSampling(long time, double vMax, double v, double vMin) {
        TimeSeriesSample s;
        if (samples.isEmpty()) {
            s = new TimeSeriesSample();
            samples.add(s);
        } else {
            s = samples.get(samples.size()-1);
        }
        if (s.getT0()>0 && (time-s.getT0())> samplingPeriod) {
            s = new TimeSeriesSample();
            samples.add(s);
        }
        s.sample(vMax, v, vMin, time);
    }

    public List<TimeSeriesSample> getSamples() {
        return samples;
    }
}
