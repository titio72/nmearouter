package com.aboni.sensors.hw;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.component.temperature.impl.TmpDS18B20DeviceType;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;

public class DS18B20 {

	private Map<String, Double> values;
	private Map<String, Double> _values;
	private boolean reading;
	
	public DS18B20() {
		values = new HashMap<String, Double>();
		_values = new HashMap<String, Double>();
		start();
	}
	
	private void start() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				_read();
			}
			
		}, 1000, 2500);
	}
	
	public void finalize() {
	}

	public void read() {}
	
	private void _read() {
		synchronized (_values) {
			if (!reading) {
				reading = true;
				_values.clear();
				W1Master master = new W1Master();
				List<W1Device> w1Devices = master.getDevices(TmpDS18B20DeviceType.FAMILY_CODE);
				for (W1Device device : w1Devices) {
			        _values.put(device.getId().trim(), ((TemperatureSensor) device).getTemperature());
				}
				reading = false;
				synchronized (values) {
					values.clear();
					values.putAll(_values);
				}
			}
		}
	}

	public double getTemp(String id) {
		synchronized (values) {
			try {
				return values.get(id).doubleValue();
			} catch (Exception e) {
				return Double.NaN;
			}
		}
	}
	
	public Map<String, Double> getValues() {
		synchronized (values) {
			return new HashMap<String, Double>(values);
		}
	}
}
