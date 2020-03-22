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
