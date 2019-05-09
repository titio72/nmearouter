package com.aboni.sensors.hw;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.component.temperature.impl.TmpDS18B20DeviceType;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;

import java.util.*;

public class DS18B20 {

	private final Map<String, Double> values;
	private final Map<String, Double> tempValues;
	private boolean reading;
	
	public DS18B20() {
		values = new HashMap<>();
		tempValues = new HashMap<>();
		start();
	}
	
	private void start() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				readData();
			}
			
		}, 1000, 2500);
	}
	
	private void readData() {
		synchronized (tempValues) {
			if (!reading) {
				reading = true;
				tempValues.clear();
				W1Master master = new W1Master();
				List<W1Device> w1Devices = master.getDevices(TmpDS18B20DeviceType.FAMILY_CODE);
				for (W1Device device : w1Devices) {
			        tempValues.put(device.getId().trim(), ((TemperatureSensor) device).getTemperature());
				}
				reading = false;
				synchronized (values) {
					values.clear();
					values.putAll(tempValues);
				}
			}
		}
	}

	public double getTemp(String id) {
		synchronized (values) {
			try {
				return values.get(id);
			} catch (Exception e) {
				return Double.NaN;
			}
		}
	}
	
	public Map<String, Double> getValues() {
		synchronized (values) {
			return new HashMap<>(values);
		}
	}
}
