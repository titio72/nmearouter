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

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.data.DataChange;
import com.aboni.nmea.router.data.StatsSample;
import com.aboni.nmea.router.data.meteo.MeteoHistory;
import com.aboni.nmea.router.data.meteo.MeteoMetrics;
import com.aboni.nmea.router.data.meteo.MeteoSampler;
import com.aboni.nmea.router.data.meteo.impl.MemoryStatsWriter;
import com.aboni.utils.Log;
import com.aboni.utils.Pair;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class NMEAMeteoMonitorTarget extends NMEAAgentImpl implements MeteoHistory {

    private final MeteoSampler meteoSampler;

    private static final int SAMPLING_FACTOR = 60; // every 60 timers dumps
    private int timerCount;

    private final Log log;

    private final MemoryStatsWriter statsWriter;

    private final String[] tags;


    private static class StatsChange {

        long referencePeriodMs;
        double rapidChangeThreshold;
        double slowChangeThreshold;

        double v0;
        double v1;
        long t0;
        long t1;

        double getInitialValue() {
            return v0;
        }

        double getFinalValue() {
            return v0;
        }

        Instant getInitialTime() {
            return Instant.ofEpochMilli(t0);
        }

        Instant getFinalTime() {
            return Instant.ofEpochMilli(t1);
        }

        double getChange() {
            return v1 - v0;
        }

        DataChange getChangeIndicator() {
            if (Math.abs(getChange()) > rapidChangeThreshold)
                return getChange() > 0 ? DataChange.RAPID_INCREASE : DataChange.RAPID_DECREASE;
            else if (Math.abs(getChange()) > slowChangeThreshold)
                return getChange() > 0 ? DataChange.SLOW_INCREASE : DataChange.SLOW_DECREASE;
            else return DataChange.STABLE;
        }
    }

    private final List<Pair<MeteoMetrics, StatsChange>> alerts;

    @Inject
    public NMEAMeteoMonitorTarget(@NotNull Log log, @NotNull NMEACache cache, @NotNull TimestampProvider tp) {
        super(log, tp, false, true);
        this.log = log;
        this.statsWriter = new MemoryStatsWriter();
        this.meteoSampler = new MeteoSampler(log, cache, tp, statsWriter, "MeteoMonitor");
        this.tags = new String[MeteoMetrics.SIZE];
        this.alerts = new ArrayList<>();
        initMetricX(MeteoMetrics.PRESSURE, 60000L, "PR_", 800.0, 1100.0);
        initMetricX(MeteoMetrics.AIR_TEMPERATURE, 60000L, "AT0", -20.0, 60.0);
        initMetricX(MeteoMetrics.HUMIDITY, 60000L, "HUM", 0.0, 150.0);
        initMetricX(MeteoMetrics.WIND_DIRECTION, 60000L, "TWD", 0.0, 100.0);
        initMetricX(MeteoMetrics.WIND_SPEED, 60000L, "TW_", -360.0, 360.0);

        initAlert(MeteoMetrics.PRESSURE, 10, 1 / 18.0, 2 / 18.0);
        initAlert(MeteoMetrics.PRESSURE, 60, 1 / 3.0, 2 / 3.0);
        initAlert(MeteoMetrics.PRESSURE, 180, 1, 2);

        initAlert(MeteoMetrics.HUMIDITY, 10, 5 / 18.0, 20 / 18.0);
        initAlert(MeteoMetrics.HUMIDITY, 60, 5 / 3.0, 20 / 3.0);
        initAlert(MeteoMetrics.HUMIDITY, 180, 5, 20);

        meteoSampler.setCollectListener(new MeteoSampler.MeteoListener() {
            @Override
            public void onCollect(MeteoMetrics metric, double value, long time) {
                for (Pair<MeteoMetrics, StatsChange> pc : alerts) {
                    if (pc.first == metric) {
                        StatsChange c = pc.second;
                        AtomicReference<Pair<Long, Double>> res = new AtomicReference<>();
                        statsWriter.scan(tags[metric.getIx()], (StatsSample s) -> {
                            if (s.getT1() > (time - c.referencePeriodMs)) {
                                return false;
                            } else {
                                res.set(new Pair<>(s.getT1(), s.getAvg()));
                                return true;
                            }
                        }, false);
                        if (res.get() != null) {
                            c.t0 = res.get().first;
                            c.t1 = time;
                            c.v0 = res.get().second;
                            c.v1 = value;
                        }
                    }
                }
            }

            @Override
            public void onSample(MeteoMetrics metric, StatsSample sample) {
                // nothing to do onSample
            }
        });
    }

    private void initAlert(MeteoMetrics metric, long periodMinutes, double slowChangeThreshold, double rapidChangeThreshold) {
        StatsChange a = new StatsChange();
        a.slowChangeThreshold = slowChangeThreshold;
        a.rapidChangeThreshold = rapidChangeThreshold;
        a.referencePeriodMs = periodMinutes * 60L * 1000L;
        alerts.add(new Pair<>(metric, a));
    }

    private void initMetricX(MeteoMetrics metric, long period, String tag, double min, double max) {
        tags[metric.getIx()] = tag;
        meteoSampler.initMetric(metric, period, tag, min, max);
    }

    @Override
    public final String getDescription() {
        return getType();
    }

    @Override
    public String getType() {
        return "Meteo monitor";
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
        timerCount = (timerCount + 1) % SAMPLING_FACTOR;
        if (timerCount == 0) dumpStats();
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

    @Override
    public List<StatsSample> getHistory(MeteoMetrics ix) {
        List<StatsSample> res = new ArrayList<>(statsWriter.getHistory(tags[ix.getIx()]));
        StatsSample current = meteoSampler.getCurrent(ix);
        if (current != null) res.add(current);
        return res;
    }

}
