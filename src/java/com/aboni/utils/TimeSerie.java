package com.aboni.utils;

import java.util.ArrayList;
import java.util.List;

public class TimeSerie {

    private final List<TimeSerieSample> samples;
    private final long samplingPeriod;

    public TimeSerie(long samplingPeriod, int initialCapacity) {
        this.samplingPeriod = samplingPeriod;
        samples = new ArrayList<>(initialCapacity);
    }

    public void doSampling(long time, double vMax, double v, double vMin) {
        TimeSerieSample s;
        if (samples.isEmpty()) {
            s = new TimeSerieSample();
            samples.add(s);
        } else {
            s = samples.get(samples.size()-1);
        }
        if (s.getT0()>0 && (time-s.getT0())> samplingPeriod) {
            s = new TimeSerieSample();
            samples.add(s);
        }
        s.sample(vMax, v, vMin, time);
    }

    public List<TimeSerieSample> getSamples() {
        return samples;
    }
}
