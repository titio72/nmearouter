package com.aboni.utils;

import java.io.IOException;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Sampler {

	private final List<Sample> samples;
	private final long sampling;
	
	public Sampler(long sampling, int initialCapacity) {
		this.sampling = sampling;
		samples = new ArrayList<>(initialCapacity);
	}
	
	public void doSampling(long time, double vMax, double v, double vMin) {
		Sample s;
		if (samples.isEmpty()) {
			s = new Sample();
			samples.add(s);
		} else {
			s = samples.get(samples.size()-1);
		}
		if (s.t0>0 && (time-s.t0)>sampling) {
			s = new Sample();
			samples.add(s);
		}
		s.sample(vMax, v, vMin, time);
	}
	
	public List<Sample> getSamples() {
		return samples;
	}
	
	public void fill(Writer w) throws IOException {
		boolean first = true;
		for (Sample s: samples) {
		    Timestamp ts = new Timestamp(s.t0);
		    double vMax = s.vMax;
		    double v = s.v;
		    double vMin = s.vMin;
			
			if (!first) {
		        w.write(",");
			}
		    w.write("{\"time\":\"" + ts.toString() + "\",");
		    w.write("\"vMin\":" + vMin + ",");
		    w.write("\"v\":" + v + ",");
		    w.write("\"vMax\":" + vMax + "}");
		    first = false;
		}		
	}
}
