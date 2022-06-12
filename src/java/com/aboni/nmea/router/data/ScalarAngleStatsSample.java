package com.aboni.nmea.router.data;

import com.aboni.misc.Utils;

public class ScalarAngleStatsSample extends StatsSample {

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

    public ScalarAngleStatsSample(String tag, double min, double max) {
        super(tag);
        this.rangeMax = max;
        this.rangeMin = min;
    }

    public ScalarAngleStatsSample(String tag) {
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
        v = Utils.normalizeDegrees180To180(v);
        vMin = Utils.normalizeDegrees180To180(vMin);
        vMax = Utils.normalizeDegrees180To180(vMax);
        if (v<0) {
            // swap
            double vTemp = vMax;
            vMax = vMin;
            vMin = vTemp;
        }
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
