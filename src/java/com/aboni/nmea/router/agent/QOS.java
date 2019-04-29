package com.aboni.nmea.router.agent;

import java.util.HashMap;
import java.util.Map;

public class QOS {

	private final Map<String, Object> qos;
	
	public QOS() {
		qos = new HashMap<>();
	}

	public void addProp(String propName) {
		qos.put(propName, 1);
	}

	public void addProp(String propName, String v) {
		qos.put(propName, v);
	}

	public boolean get(String propName) {
		return qos.containsKey(propName);
	}

	public String[] getKeys() {
		return qos.keySet().toArray(new String[] {});
	}
}
