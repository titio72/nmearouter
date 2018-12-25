package com.aboni.sensors;

import com.aboni.sensors.hw.DS18B20;
import com.aboni.utils.ServerLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SensorTemp implements Sensor {

	public class Reading {
		public String k;
		public long ts;
		public double v;
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
			ServerLog.getLogger().Error("Cannot initialize temp W1 sensor", e);
			sensor = null;
		}
	}

	@Override
	public String getSensorName() {
		return "W1TEMP";
	}

	@Override
	public long getReadAge() {
		return System.currentTimeMillis() - lastRead;
	}

	@Override
	public void read() throws SensorNotInititalizedException {
		lastRead = System.currentTimeMillis();
		if (sensor!=null) {
			//sensor.read();
			synchronized (readings) {
				Map<String, Double> m = sensor.getValues();
				for (String k : m.keySet()) {
					Reading r = new Reading();
					r.k = k;
					r.ts = lastRead;
					r.v = sensor.getTemp(k);
					readings.put(k, r);
				}
			}
		} else {
			throw new SensorNotInititalizedException("Temp sensor notr initialized!");
		}
	}

	public Collection<Reading> getReadings() {
		synchronized (readings) {
			return new ArrayList<>(readings.values());
		}
	}
}
