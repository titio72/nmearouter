package com.aboni.nmea.router.data.sampledquery.impl;

import com.aboni.nmea.router.data.sampledquery.SampledQueryConf;

public class SampledQueryConfSpeed implements SampledQueryConf {
    @Override
    public String getTable() {
        return "track";
    }

    @Override
    public String getMaxField() {
        return "maxSpeed";
    }

    @Override
    public String getMinField() {
        return "speed";
    }

    @Override
    public String getAvgField() {
        return "speed";
    }

    @Override
    public String getSeriesNameField() {
        return null;
    }

    @Override
    public String getSeriesName() {
        return "SOG";
    }
}
