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

import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.Startable;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.message.Message;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

import static com.aboni.nmea.router.data.Unit.DEGREES;

public class Sampler implements Startable {

    public interface MessageFilter {
        boolean match(Message msg);
    }

    public interface MessageValueExtractor {
        double getValue(Message msg);
    }

    public interface MeteoListener {
        void onCollect(Metric metric, double value, long time);

        void onSample(Metric metric, StatsSample sample);
    }

    public interface SamplesFilter {
        boolean accept(StatsSample sample);
    }

    private static final class Series {
        StatsSample statsSample;
        long lastStatTimeMs;
        Metric metric;
        MessageFilter filter;
        MessageValueExtractor valueExtractor;

        TimerFilter timerFilter;

        static Series getNew(@NotNull StatsSample series, @NotNull MessageFilter filter,
                             @NotNull MessageValueExtractor valueExtractor,
                             @NotNull TimerFilter timerFilter, @NotNull Metric metric) {
            if (series == null || metric == null) throw new NullPointerException("Series cannot be null");
            Series s = new Series();
            s.timerFilter = timerFilter;
            s.metric = metric;
            s.statsSample = series;
            s.filter = filter;
            s.valueExtractor = valueExtractor;
            return s;
        }
    }

    private MeteoListener collectListener;
    private final TimestampProvider timestampProvider;
    private final StatsWriter writer;
    private final String tag;
    private final Map<Metric, Series> series = new HashMap<>();
    private final Map<String, SamplesFilter> sampleFilters = new HashMap<>();
    private final Log log;
    private boolean started;

    public Sampler(@NotNull Log log, @NotNull TimestampProvider tp, StatsWriter w, @NotNull String tag) {
        this.timestampProvider = tp;
        this.log = log;
        this.writer = w;
        this.tag = tag;
    }

    public void initMetric(@NotNull Metric metric, @NotNull MessageFilter filter, @NotNull MessageValueExtractor valueExtractor,
                           long period, String tag, double min, double max) {
        initMetric(metric, filter, valueExtractor, new TimerFilterFixed(period), tag, min, max);
    }

    public void initMetric(@NotNull Metric metric, @NotNull MessageFilter filter, @NotNull MessageValueExtractor valueExtractor,
                           @NotNull TimerFilter timerFilter, String tag, double min, double max) {
        synchronized (series) {
            StatsSample sample;
            if (DEGREES == metric.getUnit()) {
                sample = new AngleStatsSample(tag);
            } else {
                sample = new ScalarStatsSample(tag, min, max);
            }
            series.put(metric, Series.getNew(sample, filter, valueExtractor, timerFilter, metric));
        }
    }

    public void setSampleFilter(@NotNull String tag, SamplesFilter filter) {
        sampleFilters.put(tag, filter);
    }

    public StatsSample getCurrent(Metric metric) {
        synchronized (series) {
            Series s = series.getOrDefault(metric, null);
            return (s == null) ? null : s.statsSample.cloneStats();
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
                    synchronized (series) {
                        long now = timestampProvider.getNow();
                        for (Series s : series.values()) {
                            if (s != null) s.lastStatTimeMs = now;
                        }
                    }
                    started = true;
                } catch (Exception e) {
                    LogStringBuilder.start("MeteoSampler").wO("activate").wV("tag", tag).errorForceStacktrace(log, e);
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
            for (Series s : series.values()) {
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

    @OnRouterMessage
    public void onSentence(RouterMessage msg) {

        if (!isStarted()) return;

        synchronized (series) {
            Message m = msg.getMessage();
            try {
                if (timestampProvider.isSynced()) {
                    for (Series ss : series.values()) {
                        if (ss != null) {
                            if (ss.filter.match(m)) {
                                process(ss.metric, ss.valueExtractor.getValue(m), msg.getTimestamp());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LogStringBuilder.start("MeteoSampler").wV("tag", tag).wO("process message").wV("message", m).error(log, e);
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
        if (writer != null && s != null && s.getSamples() > 0) {
            // skip anomalous reading (like 80kn of max with avg of 4kn)
            SamplesFilter f = sampleFilters.get(s.getTag());
            if (f == null || f.accept(s)) {
                writer.write(s, ts);
            }
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
