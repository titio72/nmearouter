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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ScalarStatsSampleX extends StatsSample {

    private final double rangeMin;
    private final double rangeMax;

    private final List<Double> theList;

    public ScalarStatsSampleX(String tag, double min, double max) {
        super(tag);
        this.rangeMax = max;
        this.rangeMin = min;
        theList = new LinkedList<>();
    }

    public double getRangeMin() {
        return rangeMin;
    }

    public double getRangeMax() {
        return rangeMax;
    }

    public void add(double v) {
        if ((Double.isNaN(getRangeMin()) || v >= getRangeMin()) &&
                (Double.isNaN(getRangeMax()) || v <= getRangeMax())) {

            int p = Collections.binarySearch(theList, v);
            if (p < 0) p = -p - 1;

            if (p == 0) min = v;
            if (p == theList.size()) max = v;
            theList.add(p, v);
        }
    }

    @Override
    public int getSamples() {
        return theList.size();
    }

    @Override
    public double getAvg() {
        if (theList.isEmpty()) return Double.NaN;
        else return theList.get(theList.size() / 2);
    }

    @Override
    public void reset() {
        super.reset();
        theList.clear();
    }
}
