package com.aboni.nmea.router.agent;

import java.util.HashMap;
import java.util.Map;

public class QOS {

	private final Map<String, Object> theQOS;
	
	public QOS() {
		theQOS = new HashMap<>();
	}

	public void addProp(String propName) {
		theQOS.put(propName, 1);
	}

	public void addProp(String propName, String v) {
		theQOS.put(propName, v);
	}

	public boolean get(String propName) {
		return theQOS.containsKey(propName);
	}

	public String[] getKeys() {
		return theQOS.keySet().toArray(new String[] {});
	}
}
