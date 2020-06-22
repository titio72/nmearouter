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

public class MagnetometerToCompass {

    public static class Calibration {
        final int x;
        final int y;
        final int z;
        final double variation;

        Calibration(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.variation = 0.0;
        }
    }

    private Calibration calibration;

    public MagnetometerToCompass() {
        setCalibration(0, 0, 0);
    }

    public void setCalibration(int x, int y , int z) {
        calibration = new Calibration(x, y, z);
    }

    private static double normOf(double[] vector) {
        if (vector == null) {
            return 0.0;
        } else {
            double res = 0.0;
            for (double v : vector) {
                res += v * v;
            }
            return Math.sqrt(res);
        }
    }

    /**
     * get the tilt compensated bearing in decimal degrees [0..360].
     *
     * @param magRaw The reading of the magnetometer
     * @param accRaw The reading of the accelerometer
     * @return The heading tilt-compensated
     */
    public double getTiltCompensatedHeading(double[] magRaw, double[] accRaw) {
        double[] mag = getCalibratedMag(magRaw);

        double accX = accRaw[0] / normOf(accRaw);
        double accY = accRaw[1] / normOf(accRaw);

        double pitch = Math.asin(accX);

        double yAngle = Math.asin(accY / Math.cos(pitch));
        double magXComp = mag[0] * Math.cos(Math.asin(accX)) + mag[2] * Math.sin(pitch);
        double magYComp = mag[0] * Math.sin(yAngle) * Math.sin(Math.asin(accX)) +
                mag[1] * Math.cos(yAngle) -
                mag[2] * Math.sin(yAngle) * Math.cos(Math.asin(accX));

        double heading = 180 * Math.atan2(magYComp, magXComp) / Math.PI;

        heading -= calibration.variation;

        if (heading < 0.0) heading += 360.0;

        return Utils.normalizeDegrees0To360(heading);
    }

    private double[] getCalibratedMag(double[] mag) {
        return new double[] {
                mag[0] - calibration.x,
                mag[1] - calibration.y,
                mag[2] - calibration.z
        };
    }


}
