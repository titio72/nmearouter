package com.aboni.nmea.router.agent;

import java.util.HashMap;
import java.util.Map;

public class QOS {

	private Map<String, Object> qos;
	
	public QOS() {
		qos = new HashMap<String, Object>();
	}

	public QOS(QOS q) {
		qos = new HashMap<String, Object>(q.qos);
	}

	public void addProp(String propName) {
		qos.put(propName, new Integer(1));
	}

	public void addProp(String propName, String v) {
		qos.put(propName, v);
	}
	
	public void addProp(String propName, double v) {
		qos.put(propName, v);
	}
	
	public void addProp(String propName, int v) {
		qos.put(propName, v);
	}
	
	public Integer getInt(String propName) throws NumberFormatException {
		if (qos.containsKey(propName)) {
			Object v = qos.get(propName);
			return Integer.parseInt(v.toString());
		} else {
			return null;
		}
	}
	
	public Double getDouble(String propName) throws NumberFormatException {
		if (qos.containsKey(propName)) {
			Object v = qos.get(propName);
			return Double.parseDouble(v.toString());
		} else {
			return null;
		}
	}

	public String getStr(String propName) throws NumberFormatException {
		if (qos.containsKey(propName)) {
			Object v = qos.get(propName);
			return v.toString();
		} else {
			return null;
		}
	}

	public boolean get(String propName) {
		return qos.containsKey(propName);
	}
}
