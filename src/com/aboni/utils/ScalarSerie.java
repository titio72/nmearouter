package com.aboni.utils;

import com.aboni.geo.Utils;

public class ScalarSerie extends Serie {
	
	private double rangeMin;
    private double rangeMax;

	public ScalarSerie(int id, String tag) {
		this(id, tag, Double.NaN, Double.NaN);
	}
	
    public ScalarSerie(int id, String tag, double min, double max) {
		super(id, tag);
		this.rangeMax = max;
		this.rangeMin = min;
	}
	
    public double getRangeMin() { return rangeMin; }
	public double getRangeMax() { return rangeMax; }

	public void add(double v) {
 		if ((Double.isNaN(getRangeMin()) || v>=getRangeMin()) && 
     		(Double.isNaN(getRangeMax()) || v<=getRangeMax())) { 
            
 			if (samples == 0) {
             	v = Utils.normalizeDegrees0_360(v);
                avg = v;
                max = v;
                min = v;
                samples = 1;
         	} else {
                avg = ((avg * samples) +  v) / (samples +1);
                max = Math.max(max,  v);
                min = Math.min(min,  v);
                samples++;
         	}
         }
     }    

	@Override
	public String getType() {
		return "Scalar[" + rangeMin + ", " + rangeMax + "]";
	}
}
