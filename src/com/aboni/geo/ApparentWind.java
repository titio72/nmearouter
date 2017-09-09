package com.aboni.geo;

public class ApparentWind {
    
    private double appWindSpeed;
    private double appWindDeg;
    
    public Double getApparentWindSpeed() {
        return appWindSpeed;
    }
    
    public Double getApparentWindDeg() {
        return appWindDeg;
    }
    
    public ApparentWind(double speed, double trueWindDeg, double trueWindSpeed) {
    	trueWindDeg = Utils.normalizeDegrees180_180(trueWindDeg);
    	double sign = (trueWindDeg<0)?-1:1;
    	trueWindDeg = Math.abs(trueWindDeg);
    	appWindSpeed = Math.sqrt(speed * speed + trueWindSpeed * trueWindSpeed + 2d * Math.cos(Math.toRadians(trueWindDeg)) * speed * trueWindSpeed);
    	appWindDeg = Math.toDegrees(
    			Math.acos((trueWindSpeed * Math.cos(Math.toRadians(trueWindDeg)) + speed) / appWindSpeed) 
    			);
    	appWindDeg = Utils.normalizeDegrees0_360(appWindDeg * sign);
    }

}
