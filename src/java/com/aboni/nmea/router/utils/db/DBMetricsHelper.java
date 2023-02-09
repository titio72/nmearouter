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

package com.aboni.nmea.router.utils.db;

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.utils.Log;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.aboni.utils.LogStringBuilder;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class DBMetricsHelper implements AutoCloseable {


    private static final String INFLUX_URL = "http://localhost:8086";
    private static final String INFLUX_DB = "nmearouter";
    private static String influxURL = INFLUX_URL;
    private static String influxDB = INFLUX_DB;
    private static InfluxDB influx = null;
    private static final Object influxSemaphore = new Object();
    private final Log log;
    public static final String DB_HELPER_CATEGORY = "DBMetricsHelper";

    public DBMetricsHelper() throws MalformedConfigurationException {
        readConf();
        log = ThingsFactory.getInstance(Log.class);
    }

    private void readConf() throws MalformedConfigurationException {
        File f = new File(Constants.DB);
        try (FileInputStream propInput = new FileInputStream(f)) {
            Properties p = new Properties();
            p.load(propInput);
            synchronized (influxSemaphore) {
                influxURL = p.getProperty("influx.url");
                influxDB = p.getProperty("influx.db");
            }
        } catch (Exception e) {
            log.debug(LogStringBuilder.start(DB_HELPER_CATEGORY).wO("Read configuration")
                    .wV("error", e.getMessage()).toString());
            throw new MalformedConfigurationException("Cannot read DB configuration", e);
        }
    }

    private static InfluxDB getInfluxStatic() {
        synchronized (influxSemaphore) {
            if (influx == null) {
                influx = InfluxDBFactory.connect(influxURL);
                influx.setDatabase(influxDB);
                influx.enableBatch(2000, 10000, TimeUnit.MILLISECONDS);
            }
        }
        return influx;
    }

    public void writeMetric(long time, String table, String metric, double value, double valueMin, double valueMax) {
        getInfluxStatic().write(Point.measurement(table)
                .time(time, TimeUnit.MILLISECONDS)
                .tag("type", metric)
                .addField("value", value)
                .addField("max", valueMax)
                .addField("min", valueMin)
                .build());
    }

    public void writeMetric(long time, String table, String metric, double value) {
        getInfluxStatic().write(Point.measurement(table)
                .time(time, TimeUnit.MILLISECONDS)
                .tag("type", metric)
                .addField("value", value)
                .build());
    }

    public InfluxDB getInflux() {
        return getInfluxStatic();
    }

    @Override
    public void close() {
        if (influx != null) influx.close();
    }


}
