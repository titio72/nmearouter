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
