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
import com.aboni.nmea.router.data.Sampler;
import com.aboni.nmea.router.data.StatsWriter;
import com.aboni.nmea.router.data.impl.TimerFilterAnchorAdaptive;
import com.aboni.nmea.router.data.impl.TimerFilterFixed;
import com.aboni.nmea.router.data.metrics.Metrics;
import com.aboni.nmea.router.message.*;
import com.aboni.nmea.router.utils.HWSettings;
import com.aboni.nmea.router.utils.Log;
import com.aboni.utils.Utils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

public class NMEAMeteoDBTarget extends NMEAAgentImpl {

    public static final long ONE_MINUTE = 60000L;
    private static final TemperatureSource AIR_TEMPERATURE_SOURCE = TemperatureSource.MAIN_CABIN_ROOM;

    private final Sampler meteoSampler;
    private final Log log;

    @Inject
    public NMEAMeteoDBTarget(@NotNull Log log, @NotNull NMEACache cache, @NotNull TimestampProvider tp,
                             @NotNull @Named(Constants.TAG_METEO) StatsWriter w) {
        super(log, tp, false, true);
        this.log = log;
        meteoSampler = new Sampler(log, tp, w, "Meteo2DB");
        meteoSampler.initMetric(Metrics.PRESSURE,
                MsgPressure.class::isInstance,
                (Message m) -> ((MsgPressure) m).getPressure(),
                new TimerFilterFixed(5 * ONE_MINUTE, 500),
                "PR_", 800.0, 1100.0);
        meteoSampler.initMetric(Metrics.WATER_TEMPERATURE,
                (Message m) -> (m instanceof MsgTemperature && TemperatureSource.SEA == ((MsgTemperature) m).getTemperatureSource()),
                (Message m) -> ((MsgTemperature) m).getTemperature(),
                new TimerFilterFixed(10 * ONE_MINUTE, 500),
                "WT_", -20.0, 60.0);
        meteoSampler.initMetric(Metrics.AIR_TEMPERATURE,
                (Message m) -> (m instanceof MsgTemperature && AIR_TEMPERATURE_SOURCE == ((MsgTemperature) m).getTemperatureSource()),
                (Message m) -> ((MsgTemperature) m).getTemperature(),
                new TimerFilterFixed(10 * ONE_MINUTE, 500),
                "AT0", -20.0, 60.0);
        meteoSampler.initMetric(Metrics.HUMIDITY,
                MsgHumidity.class::isInstance,
                (Message m) -> ((MsgHumidity) m).getHumidity(),
                new TimerFilterFixed(10 * ONE_MINUTE, 500),
                "HUM", 0.0, 150.0);
        meteoSampler.initMetric(Metrics.WIND_SPEED,
                (Message m) -> (m instanceof MsgWindData && ((MsgWindData) m).isTrue()),
                (Message m) -> ((MsgWindData) m).getSpeed(),
                new TimerFilterFixed(ONE_MINUTE, 500),
                "TW_", 0.0, 100.0);
        meteoSampler.initMetric(Metrics.WIND_DIRECTION,
                (Message m) -> (m instanceof MsgWindData && ((MsgWindData) m).isTrue() && !cache.isHeadingOlderThan(tp.getNow(), 800)),
                (Message m) -> ((MsgWindData) m).getAngle() + cache.getLastHeading().getData().getHeading(),
                new TimerFilterFixed(ONE_MINUTE, 500),
                "TWD", -360.0, 360.0);
        meteoSampler.initMetric(Metrics.ROLL,
                (Message m) -> (m instanceof MsgAttitude && !cache.isHeadingOlderThan(tp.getNow(), 800)),
                (Message m) -> Utils.normalizeDegrees180To180(((MsgAttitude) m).getRoll()) + HWSettings.getPropertyAsDouble("roll.offset", 0.0),
                new TimerFilterAnchorAdaptive(cache, ONE_MINUTE, 10 * ONE_MINUTE, 500),
                "ROL", -180.0, 180.0);
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
            log.errorForceStacktrace(() -> getLogBuilder().wO("activate").toString(), e);
            return false;
        }
    }

    @Override
    public void onTimer() {
        super.onTimer();

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
