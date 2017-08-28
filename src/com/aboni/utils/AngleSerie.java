package com.aboni.utils;

import com.aboni.geo.Utils;

public class AngleSerie extends Serie {
	
	public AngleSerie(int id, String tag) {
		super(id, tag);
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
			double a = Utils.getNormal(avg, v);
            avg = ((avg * samples) +  a) / (samples +1);
            avg = Utils.normalizeDegrees0_360(avg);
            max = Utils.normalizeDegrees0_360(Math.max(max,  a));
            min = Utils.normalizeDegrees0_360(Math.min(min,  a));
            samples++;
         }
	}

	@Override
	public String getType() {
		return "Angle";
	}
}
