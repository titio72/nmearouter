/*
 * Copyright (c) 2022,  Andrea Boni
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

package com.aboni.nmea.router.data.impl;

import com.aboni.nmea.router.data.metrics.Metric;
import com.aboni.nmea.router.data.StatScanner;
import com.aboni.nmea.router.data.StatsSample;
import com.aboni.nmea.router.data.StatsWriter;

import javax.inject.Inject;
import java.util.*;

public class MemoryStatsWriter implements StatsWriter {

    private static final List<StatsSample> EMPTY = new LinkedList<>();

    private static final long PERIOD_MS = 6 * 60 * 60 * 1000L; // 6 hours

    private final Map<String, List<StatsSample>> history;

    @Inject
    public MemoryStatsWriter() {
        history = new HashMap<>();
    }

    @Override
    public void write(StatsSample sample, long now) {
        List<StatsSample> hist = getOrCreateHistory(sample.getTag());
        synchronized (hist) {
            hist.add(sample.cloneStats());
            while (!hist.isEmpty() && hist.get(0).getT1() < (now - PERIOD_MS)) hist.remove(0);
        }
    }

    @Override
    public void init() {
        // nothing to initialize
    }

    @Override
    public void dispose() {
        // nothing to destroy
    }

    public List<StatsSample> getHistory(Metric metric) {
        List<StatsSample> res;
        synchronized (history) {
            res = history.getOrDefault(metric.getId(), null);
        }
        if (res == null) return EMPTY;
        else return res;
    }

    public void scan(Metric tag, StatScanner scanner, boolean backward) {
        List<StatsSample> list = getHistory(tag);
        synchronized (list) {
            if (list.isEmpty()) return;
            ListIterator<StatsSample> iterator = list.listIterator(backward ? (list.size() - 1) : 0);
            while (backward ? iterator.hasPrevious() : iterator.hasNext()) {
                StatsSample s = backward ? iterator.previous() : iterator.next();
                if (!scanner.scan(s)) return;
            }
        }
    }

    private List<StatsSample> getOrCreateHistory(String tag) {
        List<StatsSample> res;
        synchronized (history) {
            res = history.getOrDefault(tag, null);
            if (res == null) {
                res = new LinkedList<>();
                history.put(tag, res);
            }
        }
        return res;
    }
}
