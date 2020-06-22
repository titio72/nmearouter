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

public abstract class StatsSample {
    private final String tag;
    protected double avg;
    protected double max;
    protected double min;
    protected int samples = 0;

    public String getTag() { return tag; }

    public int getSamples() { return samples; }

    public double getAvg() { return avg; }

    public double getMin() { return min; }

    public double getMax() { return max; }

    protected StatsSample(String tag) {
        this.tag = tag;
        reset();
    }

    public void reset() {
        avg = Double.NaN;
        max = Double.NaN;
        min = Double.NaN;
        samples = 0;
    }

    public abstract void add(double v);
}
