package com.aboni.sensors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.aboni.sensors.hw.DS18B20;
import com.aboni.utils.ServerLog;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class SensorTemp implements Sensor {

	public class Reading {
		public String k;
		public long ts;
		public double v;
	}
	
	private Map<String, Reading> readings;
	
	private long lastRead;
	private DS18B20 sensor;
	
	public SensorTemp() {
		readings = new HashMap<String, Reading>();
		lastRead = 0;
		sensor = null;
	}
	
	@Override
	public void init() throws IOException, UnsupportedBusNumberException {
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
			sensor.read();
			synchronized (readings) {
				Map<String, Double> m = sensor.getValues();
				for (Iterator<String> i = m.keySet().iterator(); i.hasNext(); ) {
					String k = i.next(); 
					Reading r = new Reading();
					r.k = k;
					r.ts = lastRead;
					r.v = sensor.getTemp(k);
					readings.put(k, r);
				}
			}
		}
	}

	public double readTemp(String key) {
		synchronized (readings) {
			Reading r = readings.getOrDefault(key, new Reading());
			return r.v;
		}
	}

	public Collection<Reading> getReadings() {
		synchronized (readings) {
			return new ArrayList<Reading>(readings.values());
		}
	}
}
