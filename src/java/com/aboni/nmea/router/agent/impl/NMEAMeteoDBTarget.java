/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.*;
import com.aboni.nmea.router.data.StatsWriter;
import com.aboni.nmea.router.data.meteo.MeteoMetrics;
import com.aboni.nmea.router.data.meteo.MeteoSampler;
import com.aboni.utils.Log;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

public class NMEAMeteoDBTarget extends NMEAAgentImpl {

    private final MeteoSampler meteoSampler;

    private static final int SAMPLING_FACTOR = 60; // every 60 timers dumps
    private int timerCount;

    private final Log log;

    @Inject
    public NMEAMeteoDBTarget(@NotNull Log log, @NotNull NMEACache cache, @NotNull TimestampProvider tp,
                             @NotNull @Named(Constants.TAG_METEO) StatsWriter w) {
        super(log, tp, false, true);
        this.log = log;
        meteoSampler = new MeteoSampler(log, cache, tp, w, "Meteo2DB");
        meteoSampler.initMetric(MeteoMetrics.PRESSURE, 5 * 60000L, "PR_", 800.0, 1100.0);
        meteoSampler.initMetric(MeteoMetrics.WATER_TEMPERATURE, 10 * 60000L, "WT_", -20.0, 60.0);
        meteoSampler.initMetric(MeteoMetrics.AIR_TEMPERATURE, 10 * 60000L, "AT0", -20.0, 60.0);
        meteoSampler.initMetric(MeteoMetrics.HUMIDITY, 10 * 60000L, "HUM", 0.0, 150.0);
        meteoSampler.initMetric(MeteoMetrics.WIND_SPEED, 60000L, "TW_", 0.0, 100.0);
        meteoSampler.initMetric(MeteoMetrics.WIND_DIRECTION, 60000L, "TWD", -360.0, 360.0);
    }

    @Override
    public final String getDescription() {
        return getType();
    }

    @Override
    public String getType() {
        return "Meteo sampler DB";
    }

    @Override
    public String toString() {
        return getType();
    }

    @Override
    protected boolean onActivate() {
        try {
            meteoSampler.start();
            return true;
        } catch (Exception e) {
            getLogBuilder().wO("activate").errorForceStacktrace(log, e);
            return false;
        }
    }

    @Override
    public void onTimer() {
        super.onTimer();
        timerCount = (timerCount+1) % SAMPLING_FACTOR;
        if (timerCount==0) dumpStats();
    }

    private void dumpStats() {
        meteoSampler.dumpAndReset();
    }

    @Override
    protected void onDeactivate() {
        meteoSampler.stop();
    }

    @OnRouterMessage
    public void onSentence(RouterMessage msg) {
        meteoSampler.onSentence(msg);
    }
}
