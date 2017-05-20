package com.aboni.utils;

import java.util.List;

public class Sample {

	public double vMax = Double.NaN;
	public double v = Double.NaN;
	public double vMin = Double.NaN;
	public long t0;
	long lastTs;
	
	void sample(double vMax, double v, double vMin, long ts) {
		if (t0==0) t0 = ts;
		this.vMax = Double.isNaN(this.vMax)?vMax:Math.max(this.vMax, vMax);
		this.vMin = Double.isNaN(this.vMin)?vMin:Math.min(this.vMin, vMin);
		this.v = ts==t0?v:((this.v * (lastTs - t0)) + (v * (ts - lastTs))) / (ts - t0);
		lastTs = ts;
	}
	
	public static void doSampling(List<Sample> samples, 
			long ts, double vMax, double v, double vMin, long sampling) {
		Sample s = null;
		if (samples.isEmpty()) {
			s = new Sample();
			samples.add(s);
		} else {
			s = samples.get(samples.size()-1);
		}
		if (s.t0>0 && (ts-s.t0)>sampling) {
			s = new Sample();
			samples.add(s);
		}
		s.sample(vMax, v, vMin, ts);
	}
}
