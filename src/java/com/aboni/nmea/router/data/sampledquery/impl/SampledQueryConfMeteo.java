package com.aboni.nmea.router.data.sampledquery.impl;

import com.aboni.nmea.router.data.sampledquery.SampledQueryConf;

public class SampledQueryConfMeteo implements SampledQueryConf {

    @Override
    public String getTable() {
        return "meteo";
    }

    @Override
    public String getMaxField() {
        return "vMax";
    }

    @Override
    public String getMinField() {
        return "vMin";
    }

    @Override
    public String getAvgField() {
        return "v";
    }

    @Override
    public String getSeriesNameField() {
        return "type";
    }

    @Override
    public String getSeriesName() {
        return null;
    }
}
