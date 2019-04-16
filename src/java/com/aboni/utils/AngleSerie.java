package com.aboni.utils;

import com.aboni.misc.Utils;

public class AngleSerie extends Serie {
	
	public AngleSerie(String tag) {
		super(tag);
	}
	
	@Override
	public void add(double v) {
         if (samples == 0) {
         	v = Utils.normalizeDegrees0_360(v);
            avg = v;
            max = v;
            min = v;
            samples = 1;
         } else {
			double a = Utils.getNormal180(avg, v);
            avg = ((avg * samples) +  a) / (samples +1);
            avg = Utils.normalizeDegrees0_360(avg);
            max = Utils.normalizeDegrees0_360(Math.max(max,  a));
            min = Utils.normalizeDegrees0_360(Math.min(min,  a));
            samples++;
         }
	}
}
