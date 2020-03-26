package com.aboni.nmea.router.data.meteo;

public class MeteoSample {

    private final long ts;
    private final String tag;
    private final double maxValue;
    private final double minValue;
    private final double value;

    public static MeteoSample newInstance(long time, String tag, double min, double v, double max) {
        return new MeteoSample(time, tag, min, v, max);
    }

    private MeteoSample(long time, String tag, double min, double v, double max) {
        this.ts = time;
        this.tag = tag;
        this.minValue = min;
        this.value = v;
        this.maxValue = max;
    }

    public long getTs() {
        return ts;
    }

    public String getTag() {
        return tag;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getValue() {
        return value;
    }

}
