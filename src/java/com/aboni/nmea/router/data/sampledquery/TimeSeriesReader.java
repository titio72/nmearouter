package com.aboni.nmea.router.data.sampledquery;

import com.aboni.utils.TimeSeries;

import java.util.Map;

public interface TimeSeriesReader {
    Map<String, TimeSeries> getTimeSeries(SampledQueryConf conf, int maxSamples, Range range);
}
