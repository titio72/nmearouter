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
import com.aboni.nmea.router.data.Metrics;
import com.aboni.nmea.router.data.StatsSample;
import com.aboni.nmea.router.data.StatsWriter;
import com.aboni.nmea.router.data.meteo.MeteoSampler;
import com.aboni.nmea.router.message.*;
import com.aboni.utils.Log;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

public class NMEAMeteoDBTarget extends NMEAAgentImpl {

    private final MeteoSampler meteoSampler;

    private static final TemperatureSource AIR_TEMPERATURE_SOURCE = TemperatureSource.MAIN_CABIN_ROOM;

    private static final int SAMPLING_FACTOR = 60; // every 60 timers dumps
    private int timerCount;

    private final Log log;

    @Inject
    public NMEAMeteoDBTarget(@NotNull Log log, @NotNull NMEACache cache, @NotNull TimestampProvider tp,
                             @NotNull @Named(Constants.TAG_METEO) StatsWriter w) {
        super(log, tp, false, true);
        this.log = log;
        meteoSampler = new MeteoSampler(log, tp, w, "Meteo2DB");
        meteoSampler.initMetric(Metrics.PRESSURE,
                MsgPressure.class::isInstance,
                (Message m) -> ((MsgPressure) m).getPressure(),
                5 * 60000L, "PR_", 800.0, 1100.0);
        meteoSampler.initMetric(Metrics.WATER_TEMPERATURE,
                (Message m) -> (m instanceof MsgTemperature && TemperatureSource.SEA == ((MsgTemperature) m).getTemperatureSource()),
                (Message m) -> ((MsgTemperature) m).getTemperature(),
                10 * 60000L, "WT_", -20.0, 60.0);
        meteoSampler.initMetric(Metrics.AIR_TEMPERATURE,
                (Message m) -> (m instanceof MsgTemperature && AIR_TEMPERATURE_SOURCE == ((MsgTemperature) m).getTemperatureSource()),
                (Message m) -> ((MsgTemperature) m).getTemperature(),
                10 * 60000L, "AT0", -20.0, 60.0);
        meteoSampler.initMetric(Metrics.HUMIDITY,
                MsgHumidity.class::isInstance,
                (Message m) -> ((MsgHumidity) m).getHumidity(),
                10 * 60000L, "HUM", 0.0, 150.0);
        meteoSampler.initMetric(Metrics.WIND_SPEED,
                (Message m) -> (m instanceof MsgWindData && ((MsgWindData) m).isTrue()),
                (Message m) -> ((MsgWindData) m).getSpeed(),
                60000L, "TW_", 0.0, 100.0);
        meteoSampler.initMetric(Metrics.WIND_DIRECTION,
                (Message m) -> (m instanceof MsgWindData && ((MsgWindData) m).isTrue() && cache.isHeadingOlderThan(tp.getNow(), 800)),
                (Message m) -> ((MsgWindData) m).getAngle() + cache.getLastHeading().getData().getHeading(),
                60000L, "TWD", -360.0, 360.0);

        // prevents writing stats where the max is obviously off, like 4.5 times the average (why???? how did I come up with this???)
        meteoSampler.setSampleFilter("TW_", (StatsSample s) -> !(s.getAvg() < 10.0 && s.getMax() > (s.getAvg() * 4.5)));
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
