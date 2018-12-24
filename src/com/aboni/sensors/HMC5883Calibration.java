package com.aboni.sensors;

import com.aboni.misc.Utils;

import java.io.FileWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class HMC5883Calibration {

    private final SensorHMC5883 sensor;
    private final long timeThreshold;
    private double avgRadius;
    private static final int INTERVAL = 100;
    
    public HMC5883Calibration(SensorHMC5883 sensor, long timeout) {
        this.sensor = sensor;
        this.timeThreshold = timeout;
        sensor.setDefaultSmootingAlpha(1.0);
        
    }

    private double[] calibration;
    private double sDev;
    
    public void start() throws SensorNotInititalizedException {
        List<double[]> samples = collect();
        
        double[] min = new double[3];
        double[] max = new double[3];
        getBoundary(samples, min, max);
        
        calibration = new double[] {
                (min[0] + max[0])/2.0,      
                (min[1] + max[1])/2.0,      
                (min[2] + max[2])/2.0,      
        };
        
        double[] radius = calcConfidence(samples, calibration); 
        sDev = radius[1];
        avgRadius = radius[0];
    }

    private double[] calcConfidence(List<double[]> samples, double[] calibration) {
        Iterator<double[]> iter = samples.iterator();
        
        double[] r2s = new double[samples.size()];
        int ix = 0;
        
        while (iter.hasNext()) {
            double[] sample = iter.next();
            double r2 =  
                    Math.sqrt(
                            Math.pow(sample[0] - calibration[0], 2.0) + 
                            Math.pow(sample[1] - calibration[1], 2.0) +
                            Math.pow(sample[2] - calibration[2], 2.0));
            r2s[ix] = r2;
            ix++;
        }
        
        double r_avg = r2s[0];
        for (int i = 1; i<r2s.length; i++) {
            r_avg = r_avg * ((double)(i-1)/(double)i) + r2s[i]/(double)i; 
        }
        
        double r_sdev = 0.0;
        for (double r2 : r2s) {
            r_sdev += Math.pow(r2 - r_avg, 2);
        }
        r_sdev = Math.sqrt(r_sdev / r2s.length);
        
        
        return new double[] {r_avg, r_sdev};
    }

    private void getBoundary(List<double[]> samples, double[] min, double[] max) {
        try {
            FileWriter w = new FileWriter("cal.out");
                    
            
            Iterator<double[]> iter = samples.iterator();
            
            if (iter.hasNext()) {
                double[] sample = iter.next();
                w.write("" + sample[0] + " " + sample[1] + " " + sample[2] + "\n");
                min[0] = sample[0]; min[1] = sample[1]; min[2] = sample[2];
                max[0] = sample[0]; max[1] = sample[1]; max[2] = sample[2]; 
                while (iter.hasNext()) {
                    sample = iter.next();
                    w.write("" + sample[0] + " " + sample[1] + " " + sample[2] + "\n");
                    for (int i = 0; i<3; i++) {
                        min[i] = Math.min(min[i], sample[i]);
                        max[i] = Math.max(max[i], sample[i]);
                    }
                }
            }
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<double[]> collect() throws SensorNotInititalizedException {
        if (sensor.isInitialized()) {
            List<double[]> samples = new LinkedList<>();
            long t0 = System.currentTimeMillis();
            while ((System.currentTimeMillis() - t0) < timeThreshold) {
                try {
                    sensor.read();
                } catch (SensorNotInititalizedException e1) {
                    // should not get here
                }
                double[] d = sensor.getMagVector();
                samples.add(d);
                Utils.pause(INTERVAL);
            }
            return samples;
        } else {
            throw new SensorNotInititalizedException("Compass sensor not initialized");
        }
    }

    public double getsDev() {
        return sDev;
    }

    public double[] getCalibration() {
        return calibration;
    }
    
    public double getRadius() {
        return avgRadius;
    }
}
