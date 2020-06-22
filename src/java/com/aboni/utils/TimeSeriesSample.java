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

public class TimeSeriesSample {

    public double getValueMax() {
        return vMax;
    }

    public double getValue() {
        return v;
    }

    public double getValueMin() {
        return vMin;
    }

    public long getT0() {
        return t0;
    }

    public long getLastTs() {
        return lastTs;
    }

    private double vMax = Double.NaN;
    private double v = Double.NaN;
    private double vMin = Double.NaN;
    private long t0;
    private long lastTs;

    TimeSeriesSample() {
    }

    void sample(double vMax, double v, double vMin, long ts) {
        if (t0 == 0) t0 = ts;
        this.vMax = Double.isNaN(this.vMax) ? vMax : Math.max(this.vMax, vMax);
        this.vMin = Double.isNaN(this.vMin) ? vMin : Math.min(this.vMin, vMin);
        this.v = ts == t0 ? v : ((this.v * (lastTs - t0)) + (v * (ts - lastTs))) / (ts - t0);
        lastTs = ts;
    }

}
