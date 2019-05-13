package com.aboni.utils;

import com.aboni.misc.Utils;

public class AngleStatsSample extends StatsSample {
	
	public AngleStatsSample(String tag) {
		super(tag);
	}
	
	@Override
	public void add(double v) {
         if (samples == 0) {
         	v = Utils.normalizeDegrees0To360(v);
            avg = v;
            max = v;
            min = v;
            samples = 1;
         } else {
			double a = Utils.getNormal180(avg, v);
            avg = ((avg * samples) +  a) / (samples +1);
            avg = Utils.normalizeDegrees0To360(avg);
            max = Utils.normalizeDegrees0To360(Math.max(max,  a));
            min = Utils.normalizeDegrees0To360(Math.min(min,  a));
            samples++;
         }
	}
}
