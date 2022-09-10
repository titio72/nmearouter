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

package com.aboni.nmea.router.data.metrics.impl;

import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.data.StatsSample;
import com.aboni.nmea.router.data.StatsWriter;
import com.aboni.utils.db.DBHelper;

import javax.inject.Inject;

public class DBMetricStatsWriterInflux implements StatsWriter {

    private DBHelper helper;

    @Inject
    public DBMetricStatsWriterInflux() {
    }

    @Override
    public void init() {
        try {
            helper = new DBHelper(true);
        } catch (ClassNotFoundException | MalformedConfigurationException e) {
            helper = null;
            // TODO
        }
    }

    @Override
    public void write(StatsSample s, long ts) {
        if (helper != null) {
            helper.writeMetric(ts, "meteo", s.getTag(), s.getAvg(), s.getMin(), s.getMax());
        }
    }

    @Override
    public void dispose() {
        try {
            if (helper != null) helper.close();
        } catch (Exception e) {
            // TODO
        }
    }
}
