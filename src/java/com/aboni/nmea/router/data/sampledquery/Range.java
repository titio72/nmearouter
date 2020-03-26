package com.aboni.nmea.router.data.sampledquery;

import java.time.Instant;

public class Range {
    private final Instant max;
    private final Instant min;
    private final long count;

    public Range(Instant max, Instant min, long count) {
        this.max = max;
        this.min = min;
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public long getInterval() {
        return max.toEpochMilli() - min.toEpochMilli();
    }

    public int getSampling(int maxSamples) {
        return (int) ((getCount() <= maxSamples) ? 1 : (getInterval() / maxSamples));
    }

    public Instant getMax() {
        return max;
    }

    public Instant getMin() {
        return min;
    }

}
