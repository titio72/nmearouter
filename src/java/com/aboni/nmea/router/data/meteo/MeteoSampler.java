/*
 * Copyright (c) 2020,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.data.meteo;

import com.aboni.nmea.router.*;
import com.aboni.nmea.router.data.AngleStatsSample;
import com.aboni.nmea.router.data.ScalarStatsSample;
import com.aboni.nmea.router.data.StatsSample;
import com.aboni.nmea.router.data.StatsWriter;
import com.aboni.nmea.router.message.*;
import com.aboni.utils.DataEvent;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;

import javax.validation.constraints.NotNull;
import java.util.Arrays;

public class MeteoSampler implements Startable {

    public interface MeteoListener {
        void onCollect(MeteoMetrics metric, double value, long time);

        void onSample(MeteoMetrics metric, StatsSample sample);
    }

    private MeteoListener collectListener;

    private final TimestampProvider timestampProvider;
    private final NMEACache cache;
    private final StatsWriter writer;
    private final String tag;

    private final StatsSample[] series = new StatsSample[MeteoMetrics.SIZE];
    private final long[] periodsMs = new long[MeteoMetrics.SIZE];
    private final long[] lastStatTimeMs = new long[MeteoMetrics.SIZE];

    private final Log log;

    private boolean started;

    public MeteoSampler(@NotNull Log log, @NotNull NMEACache cache, @NotNull TimestampProvider tp, StatsWriter w, @NotNull String tag) {
        this.timestampProvider = tp;
        this.cache = cache;
        this.log = log;
        this.writer = w;
        this.tag = tag;
    }

    public void initMetric(MeteoMetrics metric, long period, String tag, double min, double max) {
        synchronized (series) {
            periodsMs[metric.getIx()] = period;
            if (metric == MeteoMetrics.WIND_DIRECTION)
                series[metric.getIx()] = new AngleStatsSample(tag);
            else
                series[metric.getIx()] = new ScalarStatsSample(tag, min, max);
        }
    }

    public StatsSample getCurrent(MeteoMetrics metric) {
        synchronized (series) {
            return (series[metric.getIx()] != null) ? series[metric.getIx()].cloneStats() : null;
        }
    }

    public void setCollectListener(MeteoListener collectListener) {
        this.collectListener = collectListener;
    }

    @Override
    public void start() {
        synchronized (this) {
            if (!started) {
                try {
                    if (writer != null) writer.init();
                    Arrays.fill(lastStatTimeMs, timestampProvider.getNow());
                    started = true;
                } catch (Exception e) {
                    LogStringBuilder.start("MeteoSampler").wO("activate").wV("tag", tag).errorForceStacktrace(log, e);
                    started = false;
                }
            }
        }
    }

    public void dumpAndReset() {
        synchronized (series) {
            long ts = timestampProvider.getNow();
            for (int i = 0; i < series.length; i++) {
                StatsSample series1 = series[i];
                if (series1 != null && (ts - lastStatTimeMs[i]) >= periodsMs[i]) {
                    write(series1, ts);
                    if (collectListener != null) {
                        collectListener.onSample(MeteoMetrics.valueOf(i), series1);
                    }
                    series1.reset();
                    lastStatTimeMs[i] = ts;
                }
            }
        }
    }

    @Override
    public void stop() {
        synchronized (this) {
            if (isStarted() && writer != null) {
                started = false;
                writer.dispose();
            }
        }
    }

    @Override
    public boolean isStarted() {
        synchronized (this) {
            return started;
        }
    }

    @OnRouterMessage
    public void onSentence(RouterMessage msg) {

        if (!isStarted()) return;

        synchronized (series) {
            Message m = msg.getMessage();
            try {
                if (Boolean.TRUE.equals(cache.getStatus(NMEARouterStatuses.GPS_TIME_SYNC, false))) {
                    if (m instanceof MsgTemperature &&
                            TemperatureSource.MAIN_CABIN_ROOM == ((MsgTemperature) m).getTemperatureSource()) {
                        processTemp(((MsgTemperature) m).getTemperature(), msg.getTimestamp());
                    }

                    if (m instanceof MsgPressure) {
                        processPressure(((MsgPressure) m).getPressure(), msg.getTimestamp());
                    }

                    if (m instanceof MsgTemperature &&
                            TemperatureSource.SEA == ((MsgTemperature) m).getTemperatureSource()) {
                        processWaterTemp(((MsgTemperature) m).getTemperature(), msg.getTimestamp());
                    }

                    if (m instanceof MsgHumidity) {
                        processHumidity(((MsgHumidity) m).getHumidity(), msg.getTimestamp());
                    }

                    if (m instanceof MsgWindData) {
                        processWind((MsgWindData) m, msg.getTimestamp());
                    }
                }
            } catch (Exception e) {
                LogStringBuilder.start("MeteoSampler").wV("tag", tag).wO("process message").wV("message", m).error(log, e);
            }
        }
    }

    private void collect(MeteoMetrics id, double d, long time) {
        synchronized (series) {
            StatsSample s = series[id.getIx()];
            if (s != null) {
                s.add(d, time);
            }
        }
    }

    private void write(StatsSample s, long ts) {
        if (writer != null && s != null && s.getSamples() > 0) {
            if ("TW_".equals(s.getTag()) && s.getAvg() < 10.0 && s.getMax() > (s.getAvg() * 4.5)) {
                // skip anomalous reading (like 80kn of max with avg of 4kn)
                return;
            }
            writer.write(s, ts);
        }
    }

    private void processWind(MsgWindData s, long time) {
        if (s.isTrue()) {
            DataEvent<MsgHeading> e = cache.getLastHeading();
            if (!cache.isHeadingOlderThan(timestampProvider.getNow(), 800)) {
                double windDir = e.getData().getHeading() + s.getAngle();
                double windSpd = s.getSpeed();
                collect(MeteoMetrics.WIND_SPEED, windSpd, time);
                collect(MeteoMetrics.WIND_DIRECTION, windDir, time);
                if (collectListener != null) {
                    collectListener.onCollect(MeteoMetrics.WIND_SPEED, windSpd, time);
                    collectListener.onCollect(MeteoMetrics.WIND_DIRECTION, windDir, time);
                }
            }
        }
    }

    private void processWaterTemp(double temperature, long time) {
        if (!Double.isNaN(temperature)) {
            collect(MeteoMetrics.WATER_TEMPERATURE, temperature, time);
            if (collectListener != null) {
                collectListener.onCollect(MeteoMetrics.WATER_TEMPERATURE, temperature, time);
            }
        }
    }

    private void processPressure(double pressure, long time) {
        if (!Double.isNaN(pressure)) {
            collect(MeteoMetrics.PRESSURE, pressure, time);
            if (collectListener != null) {
                collectListener.onCollect(MeteoMetrics.PRESSURE, pressure, time);
            }
        }
    }

    private void processTemp(double temperature, long time) {
        if (!Double.isNaN(temperature)) {
            collect(MeteoMetrics.AIR_TEMPERATURE, temperature, time);
            if (collectListener != null) {
                collectListener.onCollect(MeteoMetrics.AIR_TEMPERATURE, temperature, time);
            }
        }
    }

    private void processHumidity(double humidity, long time) {
        if (!Double.isNaN(humidity)) {
            collect(MeteoMetrics.HUMIDITY, humidity, time);
            if (collectListener != null) {
                collectListener.onCollect(MeteoMetrics.HUMIDITY, humidity, time);
            }
        }
    }
}
