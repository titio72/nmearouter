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

package com.aboni.sensors;

import com.aboni.sensors.hw.DS18B20;
import com.aboni.log.Log;
import com.aboni.log.LogStringBuilder;
import com.aboni.log.SafeLog;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SensorTemp implements Sensor {

    public static class Reading {

        private Reading(String key, long timestamp, double value) {
            k = key;
            ts = timestamp;
            v = value;
        }

        private final String k;
        private final long ts;
        private final double v;

        public String getKey() {
            return k;
        }

        public double getValue() {
            return v;
        }

        public long getTimestamp() {
            return ts;
        }
    }

    private final Map<String, Reading> readings;

    private long lastRead;
    private DS18B20 sensor;
    private final Log log;

    @Inject
    public SensorTemp(Log log) {
        readings = new HashMap<>();
        lastRead = 0;
        sensor = null;
        this.log = SafeLog.getSafeLog(log);
    }

    @Override
    public void init() {
        try {
            sensor = new DS18B20();
        } catch (Exception e) {
            log.error(() -> LogStringBuilder.start("DS18B20Sensor").wO("init").toString(), e);
            sensor = null;
        }
    }

    @Override
    public String getSensorName() {
        return "W1TEMP";
    }

    @Override
    public void read() throws SensorNotInitializedException {
        lastRead = System.currentTimeMillis();
        if (sensor != null) {
            synchronized (readings) {
                Map<String, Double> m = sensor.getValues();
                for (String k : m.keySet()) {
                    Reading r = new Reading(k, lastRead, sensor.getTemp(k));
                    readings.put(k, r);
                }
            }
        } else {
            throw new SensorNotInitializedException("Temp sensor not initialized!");
        }
    }

    public Collection<Reading> getReadings() {
        synchronized (readings) {
            return new ArrayList<>(readings.values());
        }
    }

    @Override
    public long getLastReadingTimestamp() {
        return lastRead;
    }
}
