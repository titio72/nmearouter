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

package com.aboni.nmea.router.data.power.impl;

import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.data.Sample;
import com.aboni.nmea.router.data.StatsWriter;
import com.aboni.nmea.router.utils.db.DBMetricsHelper;

import javax.inject.Inject;

public class DBPowerStatsWriterInflux implements StatsWriter {

    private DBMetricsHelper helper;

    @Inject
    public DBPowerStatsWriterInflux() {
        // nothing to init
    }

    @Override
    public void init() {
        try {
            helper = new DBMetricsHelper();
        } catch (MalformedConfigurationException e) {
            helper = null;
            // TODO
        }
    }

    @Override
    public void write(Sample s, long ts) {
        if (helper != null) {
            helper.writeMetric(ts, "battery", s.getTag(), s.getValue());
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
