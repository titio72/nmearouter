package com.aboni.sensors;

import com.aboni.misc.Utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class CompassCalibration {

    private final SensorHMC5883 sensor;
    private final long timeThreshold;
    private double avgRadius;
    private static final int INTERVAL = 50;
    
    public CompassCalibration(SensorHMC5883 sensor, long timeout) {
        this.sensor = sensor;
        this.timeThreshold = timeout;
        
    }

    private double[] calibration;
    private double sDev;
    
    public void start() throws SensorException {
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
        Iterator<double[]> iterator = samples.iterator();

        double[] r2s = new double[samples.size()];
        int ix = 0;

        while (iterator.hasNext()) {
            double[] sample = iterator.next();
            double r2 =
                    Math.sqrt(
                            Math.pow(sample[0] - calibration[0], 2.0) +
                                    Math.pow(sample[1] - calibration[1], 2.0) +
                                    Math.pow(sample[2] - calibration[2], 2.0));
            r2s[ix] = r2;
            ix++;
        }

        double rAvg = r2s[0];
        for (int i = 1; i<r2s.length; i++) {
            rAvg = rAvg * ((double)(i-1)/(double)i) + r2s[i]/(double)i;
        }

        double rStdDev = 0.0;
        for (double r2 : r2s) {
            rStdDev += Math.pow(r2 - rAvg, 2);
        }
        rStdDev = Math.sqrt(rStdDev / r2s.length);


        return new double[]{rAvg, rStdDev};
    }

    private void getBoundary(List<double[]> samples, double[] min, double[] max) {
        Iterator<double[]> iterator = samples.iterator();
        if (iterator.hasNext()) {
            double[] sample = iterator.next();
            min[0] = sample[0];
            min[1] = sample[1];
            min[2] = sample[2];
            max[0] = sample[0];
            max[1] = sample[1];
            max[2] = sample[2];
            while (iterator.hasNext()) {
                sample = iterator.next();
                for (int i = 0; i < 3; i++) {
                    min[i] = Math.min(min[i], sample[i]);
                    max[i] = Math.max(max[i], sample[i]);
                }
            }
        }
    }

    private List<double[]> collect() throws SensorException {
        if (sensor.isInitialized()) {
            List<double[]> samples = new LinkedList<>();
            long t0 = System.currentTimeMillis();
            while ((System.currentTimeMillis() - t0) < timeThreshold) {
                try {
                    sensor.read();
                } catch (SensorNotInitializedException e1) {
                    // should not get here
                }
                double[] d = sensor.getMagVector();
                samples.add(d);
                Utils.pause(INTERVAL);
            }
            return samples;
        } else {
            throw new SensorNotInitializedException("Compass sensor not initialized");
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
