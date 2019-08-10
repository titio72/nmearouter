package com.aboni.utils;

public class ScalarStatsSample extends StatsSample {
	
	private final double rangeMin;
    private final double rangeMax;

    public ScalarStatsSample(String tag, double min, double max) {
		super(tag);
		this.rangeMax = max;
		this.rangeMin = min;
	}
	
    public double getRangeMin() { return rangeMin; }
	public double getRangeMax() { return rangeMax; }

	public void add(double v) {
 		if ((Double.isNaN(getRangeMin()) || v>=getRangeMin()) && 
     		(Double.isNaN(getRangeMax()) || v<=getRangeMax())) { 
            
 			if (samples == 0) {
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
}
