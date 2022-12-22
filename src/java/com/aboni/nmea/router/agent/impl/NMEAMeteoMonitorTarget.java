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
import com.aboni.nmea.router.data.HistoryProvider;
import com.aboni.nmea.router.data.Sampler;
import com.aboni.nmea.router.data.StatsSample;
import com.aboni.nmea.router.data.impl.MemoryStatsWriter;
import com.aboni.nmea.router.data.metrics.Metric;
import com.aboni.nmea.router.data.metrics.Metrics;
import com.aboni.nmea.router.message.*;
import com.aboni.utils.Log;
import com.aboni.utils.Pair;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class NMEAMeteoMonitorTarget extends NMEAAgentImpl implements HistoryProvider {

    private final Sampler meteoSampler;

    private static final TemperatureSource AIR_TEMPERATURE_SOURCE = TemperatureSource.MAIN_CABIN_ROOM;

    private static final int SAMPLING_FACTOR = 60; // every 60 timers dumps
    private int timerCount;

    private final Log log;

    private final MemoryStatsWriter statsWriter;

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

    private final List<Pair<Metric, StatsChange>> alerts;

    private class AlertManager implements Sampler.MetricListener {

        @Override
        public void onCollect(Metric metric, double value, long time) {
            for (Pair<Metric, StatsChange> pc : alerts) {
                if (pc.first == metric) {
                    StatsChange c = pc.second;
                    AtomicReference<Pair<Long, Double>> res = new AtomicReference<>();
                    statsWriter.scan(metric, (StatsSample s) -> {
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
        public void onSample(Metric metric, StatsSample sample) {
            try {
                if (sample.getSamples() > 0 && !Double.isNaN(sample.getAvg())) {
                    JSONObject msg = new JSONObject();
                    msg.put("topic", "meteo_sample");
                    msg.put("tag", sample.getTag());
                    msg.put("min", sample.getMin());
                    msg.put("avg", sample.getAvg());
                    msg.put("max", sample.getMax());
                    msg.put("samples", sample.getSamples());
                    msg.put("time", sample.getT0());
                    NMEAMeteoMonitorTarget.this.postMessage(msg);
                }
            } catch (JSONException e) {
                getLogBuilder().wO("send_sample").wV("error", "error converting meteo sample to JSON").error(log, e);
            }
        }
    }

    @Inject
    public NMEAMeteoMonitorTarget(@NotNull Log log, @NotNull NMEACache cache, @NotNull TimestampProvider tp) {
        super(log, tp, false, true);
        this.log = log;
        this.statsWriter = new MemoryStatsWriter();
        this.meteoSampler = new Sampler(log, tp, statsWriter, "MeteoMonitor");
        this.alerts = new ArrayList<>();

        initMetricX(Metrics.PRESSURE,
                MsgPressure.class::isInstance,
                (Message m) -> ((MsgPressure) m).getPressure(),
                800.0, 1100.0);
        initMetricX(Metrics.AIR_TEMPERATURE,
                (Message m) -> (m instanceof MsgTemperature && AIR_TEMPERATURE_SOURCE == ((MsgTemperature) m).getTemperatureSource()),
                (Message m) -> ((MsgTemperature) m).getTemperature(),
                -20.0, 60.0);
        initMetricX(Metrics.HUMIDITY,
                MsgHumidity.class::isInstance,
                (Message m) -> ((MsgHumidity) m).getHumidity(),
                0.0, 150.0);
        initMetricX(Metrics.WIND_DIRECTION,
                (Message m) -> (m instanceof MsgWindData && ((MsgWindData) m).isTrue() && !cache.isHeadingOlderThan(tp.getNow(), 800)),
                (Message m) -> ((MsgWindData) m).getAngle() + cache.getLastHeading().getData().getHeading(),
                -360.0, 360.0);
        initMetricX(Metrics.WIND_SPEED,
                (Message m) -> (m instanceof MsgWindData && ((MsgWindData) m).isTrue()),
                (Message m) -> ((MsgWindData) m).getSpeed(),
                0, 100.0);

        initAlert(Metrics.PRESSURE, 10, 1 / 18.0, 2 / 18.0);
        initAlert(Metrics.PRESSURE, 60, 1 / 3.0, 2 / 3.0);
        initAlert(Metrics.PRESSURE, 180, 1, 2);

        initAlert(Metrics.HUMIDITY, 10, 5 / 18.0, 20 / 18.0);
        initAlert(Metrics.HUMIDITY, 60, 5 / 3.0, 20 / 3.0);
        initAlert(Metrics.HUMIDITY, 180, 5, 20);

        meteoSampler.setCollectListener(new AlertManager());
    }

    private void initAlert(Metric metric, long periodMinutes, double slowChangeThreshold, double rapidChangeThreshold) {
        StatsChange a = new StatsChange();
        a.slowChangeThreshold = slowChangeThreshold;
        a.rapidChangeThreshold = rapidChangeThreshold;
        a.referencePeriodMs = periodMinutes * 60L * 1000L;
        alerts.add(new Pair<>(metric, a));
    }

    private void initMetricX(Metric metric,
                             Sampler.MessageFilter filter,
                             Sampler.MessageValueExtractor valueExtractor,
                             double min, double max) {
        meteoSampler.initMetric(metric, filter, valueExtractor, 60000L, metric.getId(), min, max);
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
        if (timerCount == 0 && isStarted()) dumpStats();
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
    public List<StatsSample> getHistory(Metric ix) {
        List<StatsSample> h = statsWriter.getHistory(ix);
        List<StatsSample> res = new ArrayList<>(h);
        StatsSample current = meteoSampler.getCurrent(ix);
        if (current != null) res.add(current);
        return res;
    }
}
