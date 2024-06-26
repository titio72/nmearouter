/*
 * Copyright (c) 2021,  Andrea Boni
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

package com.aboni.nmea.router.data;

import com.aboni.nmea.router.data.impl.AngleStatsSample;
import com.aboni.nmea.router.data.impl.ScalarStatsSample;
import com.aboni.nmea.router.data.impl.TimerFilterFixed;
import com.aboni.utils.TimestampProvider;
import com.aboni.nmea.router.data.metrics.Metric;
import com.aboni.log.Log;
import com.aboni.log.LogStringBuilder;
import com.aboni.utils.Startable;

import java.util.HashMap;
import java.util.Map;

public class Sampler<T> implements Startable {

    public interface MessageFilter<X> {
        boolean match(X msg);
    }

    public interface MessageValueExtractor<X> {
        double getValue(X msg);
    }

    public interface MetricListener {
        void onCollect(Metric metric, double value, long time);

        void onSample(Metric metric, StatsSample sample);
    }

    private static final class Series<X> {
        StatsSample statsSample;
        long lastStatTimeMs;
        Metric metric;
        MessageFilter<X> filter;
        MessageValueExtractor<X> valueExtractor;

        TimerFilter timerFilter;

        static<X> Series<X> getNew(StatsSample series, MessageFilter<X> filter,
                             MessageValueExtractor<X> valueExtractor,
                             TimerFilter timerFilter, Metric metric) {
            Series<X> s = new Series<>();
            s.timerFilter = timerFilter;
            s.metric = metric;
            s.statsSample = series;
            s.filter = filter;
            s.valueExtractor = valueExtractor;
            return s;
        }
    }

    private MetricListener collectListener;
    private final TimestampProvider timestampProvider;
    private final StatsWriter writer;
    private final String tag;
    private final Map<Metric, Series<T>> series = new HashMap<>();
    private final Log log;
    private boolean started;

    public Sampler(Log log, TimestampProvider tp, StatsWriter w, String tag) {
        this.timestampProvider = tp;
        this.log = log;
        this.writer = w;
        this.tag = tag;
    }

    public void initMetric(Metric metric, MessageFilter<T> filter, MessageValueExtractor<T> valueExtractor,
                           long period, String tag, double min, double max) {
        initMetric(metric, filter, valueExtractor, new TimerFilterFixed(period), tag, min, max);
    }

    public void initMetric(Metric metric, MessageFilter<T> filter, MessageValueExtractor<T> valueExtractor,
                           TimerFilter timerFilter, String tag, double min, double max) {
        synchronized (series) {
            StatsSample sample;
            if (Unit.DEGREES == metric.getUnit()) {
                sample = new AngleStatsSample(tag);
            } else {
                sample = new ScalarStatsSample(tag, min, max);
            }
            series.put(metric, Series.getNew(sample, filter, valueExtractor, timerFilter, metric));
        }
    }

    public StatsSample getCurrent(Metric metric) {
        synchronized (series) {
            Series<T> s = series.getOrDefault(metric, null);
            return (s == null) ? null : s.statsSample.cloneStats();
        }
    }

    public void setCollectListener(MetricListener collectListener) {
        this.collectListener = collectListener;
    }

    @Override
    public void start() {
        synchronized (this) {
            if (!started) {
                try {
                    if (writer != null) writer.init();
                    synchronized (series) {
                        long now = timestampProvider.getNow();
                        for (Series<T> s : series.values()) {
                            if (s != null) s.lastStatTimeMs = now;
                        }
                    }
                    started = true;
                } catch (Exception e) {
                    log.errorForceStacktrace(() -> LogStringBuilder.start("MeteoSampler").wO("activate").wV("tag", tag).toString(), e);
                    started = false;
                }
            }
        }
    }

    public void dumpAndReset() {
        dumpAndReset(false);
    }

    public void dumpAndReset(boolean force) {
        synchronized (series) {
            long ts = timestampProvider.getNow();
            for (Series<T> s : series.values()) {
                if (s != null) {
                    StatsSample statsSample = s.statsSample;
                    if (statsSample != null && (force || s.timerFilter.accept(s.lastStatTimeMs, ts))) {
                        write(statsSample, ts);
                        if (collectListener != null) {
                            collectListener.onSample(s.metric, statsSample);
                        }
                        statsSample.reset();
                        s.lastStatTimeMs = ts;
                    }
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

    public void doSampling(T m, long timestamp) {
        if (!isStarted()) return;
        synchronized (series) {
            try {
                if (timestampProvider.isSynced()) {
                    for (Series<T> ss : series.values()) {
                        if (ss != null && ss.filter.match(m)) {
                            process(ss.metric, ss.valueExtractor.getValue(m), timestamp);
                        }
                    }
                }
            } catch (Exception e) {
                log.error(() -> LogStringBuilder.start("MeteoSampler").wV("tag", tag).wO("process message").wV("message", m).toString(), e);
            }
        }
    }

    private void collect(Metric id, double d, long time) {
        synchronized (series) {
            if (series.containsKey(id)) {
                StatsSample s = series.get(id).statsSample;
                s.add(d, time);
            }
        }
    }

    private void write(StatsSample s, long ts) {
        log.info("writer" + writer + " " + s.getSamples());
        if (writer != null && s.getSamples() > 0) {
            writer.write(s.getImmutableCopy(), ts);
        }
    }

    private void process(Metric metric, double value, long time) {
        if (!Double.isNaN(value)) {
            collect(metric, value, time);
            if (collectListener != null) {
                collectListener.onCollect(metric, value, time);
            }
        }
    }
}
