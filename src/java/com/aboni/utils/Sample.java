package com.aboni.utils;

public class Sample {

	public double getvMax() {
		return vMax;
	}

	public double getV() {
		return v;
	}

	public double getvMin() {
		return vMin;
	}

	public long getT0() {
		return t0;
	}

	public long getLastTs() {
		return lastTs;
	}

	private double vMax = Double.NaN;
	private double v = Double.NaN;
	private double vMin = Double.NaN;
	private long t0;
	private long lastTs;

	Sample() {}

	void sample(double vMax, double v, double vMin, long ts) {
		if (t0==0) t0 = ts;
		this.vMax = Double.isNaN(this.vMax)?vMax:Math.max(this.vMax, vMax);
		this.vMin = Double.isNaN(this.vMin)?vMin:Math.min(this.vMin, vMin);
		this.v = ts==t0?v:((this.v * (lastTs - t0)) + (v * (ts - lastTs))) / (ts - t0);
		lastTs = ts;
	}

}
