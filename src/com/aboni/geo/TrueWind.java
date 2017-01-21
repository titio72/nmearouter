package com.aboni.geo;

public class TrueWind {
    
    private double trueWindSpeed;
    private double trueWindDeg;
    
    public Double getTrueWindSpeed() {
        return trueWindSpeed;
    }
    
    public Double getTrueWindDeg() {
        return trueWindDeg;
    }
    
    public TrueWind(double speed, double appWindDeg, double appWindSpeed) {
        double v = speed;
        double w = appWindSpeed;
        double wA =  Math.toRadians(appWindDeg);
        
        double wAx = w * Math.sin(wA);
        double wAy = w * Math.cos(wA);
        
        double wTx = wAx;
        double wTy = wAy - v;
        
        trueWindDeg = Math.toDegrees((Math.PI / 2) - Math.atan2(wTy, wTx));
        trueWindSpeed = Math.sqrt(wTx*wTx + wTy*wTy);
    }

}
