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
