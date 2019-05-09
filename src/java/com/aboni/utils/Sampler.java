package com.aboni.utils;

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
		if (s.getT0()>0 && (time-s.getT0())>sampling) {
			s = new Sample();
			samples.add(s);
		}
		s.sample(vMax, v, vMin, time);
	}
	
	public List<Sample> getSamples() {
		return samples;
	}
}
