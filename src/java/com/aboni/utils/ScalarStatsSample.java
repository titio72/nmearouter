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

public class ScalarStatsSample extends StatsSample {

    private final double rangeMin;
    private final double rangeMax;

    public ScalarStatsSample(String tag, double min, double max) {
        super(tag);
        this.rangeMax = max;
        this.rangeMin = min;
    }

    public double getRangeMin() { return rangeMin; }

    public double getRangeMax() { return rangeMax; }

    public void add(double v) {
        if ((Double.isNaN(getRangeMin()) || v>=getRangeMin()) &&
                (Double.isNaN(getRangeMax()) || v<=getRangeMax())) {

            if (samples == 0) {
                avg = v;
                max = v;
                min = v;
                samples = 1;
            } else {
                avg = ((avg * samples) +  v) / (samples +1);
                max = Math.max(max,  v);
                min = Math.min(min,  v);
                samples++;
            }
        }
    }
}
