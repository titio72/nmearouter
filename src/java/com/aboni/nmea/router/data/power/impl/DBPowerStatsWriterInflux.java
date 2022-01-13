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

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.data.StatsSample;
import com.aboni.nmea.router.data.StatsWriter;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

public class DBPowerStatsWriterInflux implements StatsWriter {

    private InfluxDB influx;
    private final Log log;
    private final String tag;

    @Inject
    public DBPowerStatsWriterInflux(@NotNull Log log, @NotNull @Named(Constants.TAG_POWER) String tag) {
        this.log = log;
        this.tag = tag;
    }

    @Override
    public void init() {
        if (influx == null) {
            try {
                influx = InfluxDBFactory.connect("http://localhost:8086");
            } catch (Exception e) {
                LogStringBuilder.start("DBPowerStatsWriterInflux").wV("type", tag).wO("init").error(log, e);
            }
        }
    }

    @Override
    public void write(StatsSample s, long ts) {
        if (influx != null) {
            influx.write(Point.measurement("battery")
                    .time(ts, TimeUnit.MILLISECONDS)
                    .tag("type", s.getTag())
                    .addField("value", s.getAvg())
                    .build());
        }
    }

    @Override
    public void dispose() {
        if (influx != null) {
            influx.close();
            influx = null;
        }
    }
}
