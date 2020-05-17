package com.aboni.sensors;

import com.aboni.sensors.hw.DS18B20;
import com.aboni.utils.ServerLog;

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
	
	public SensorTemp() {
		readings = new HashMap<>();
		lastRead = 0;
		sensor = null;
	}
	
	@Override
	public void init() {
		try {
			sensor = new DS18B20();
		} catch (Exception e) {
			ServerLog.getLogger().error("Cannot initialize temp W1 sensor", e);
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
