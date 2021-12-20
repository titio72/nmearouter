/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

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
        sensor.setDefaultSmoothingAlpha(1.0);

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
            rAvg = rAvg * ((double) (i - 1) / (double) i) + r2s[i] / i;
        }

        double rStdDev = 0.0;
        for (double r2 : r2s) {
            rStdDev += Math.pow(r2 - rAvg, 2);
        }
        rStdDev = Math.sqrt(rStdDev / r2s.length);


        return new double[]{rAvg, rStdDev};
    }

    private void getBoundary(List<double[]> samples, double[] min, double[] max) throws SensorException {
        try {
            try (FileWriter w = new FileWriter("cal.out")) {


                Iterator<double[]> iterator = samples.iterator();

                if (iterator.hasNext()) {
                    double[] sample = iterator.next();
                    w.write("" + sample[0] + " " + sample[1] + " " + sample[2] + "\n");
                    min[0] = sample[0];
                    min[1] = sample[1];
                    min[2] = sample[2];
                    max[0] = sample[0];
                    max[1] = sample[1];
                    max[2] = sample[2];
                    while (iterator.hasNext()) {
                        sample = iterator.next();
                        w.write("" + sample[0] + " " + sample[1] + " " + sample[2] + "\n");
                        for (int i = 0; i < 3; i++) {
                            min[i] = Math.min(min[i], sample[i]);
                            max[i] = Math.max(max[i], sample[i]);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new SensorException("Error checking boundaries for HMC5883 Calibration", e);
        }
    }

    private List<double[]> collect() throws SensorException {
        if (sensor.isInitialized()) {
            List<double[]> samples = new LinkedList<>();
            long t0 = System.currentTimeMillis();
            while ((System.currentTimeMillis() - t0) < timeThreshold) {
                sensor.readSensor();
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
