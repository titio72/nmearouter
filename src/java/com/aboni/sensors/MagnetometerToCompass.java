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
    * @param mag_raw The reading of the magnetometer
    * @param acc_raw The reading of the accelerometer
    * @return The heading tilt-compnsated
    */
   public double getTiltCompensatedHeading(double[] mag_raw, double[] acc_raw) {
       double[] mag = getCalibratedMag(mag_raw);

       double accX = acc_raw[0] / normOf(acc_raw);
       double accY = acc_raw[1] / normOf(acc_raw);

       double pitch = Math.asin(accX);
       //double roll = -Math.asin(accYnorm / Math.cos(pitch));
       
       double magXcomp = mag[0] * Math.cos(Math.asin(accX)) + mag[2] * Math.sin(pitch);
       double magYcomp = mag[0] * Math.sin(Math.asin(accY / Math.cos(pitch))) * Math.sin(Math.asin(accX)) + 
               mag[1] * Math.cos(Math.asin(accY / Math.cos(pitch))) - 
               mag[2] * Math.sin(Math.asin(accY / Math.cos(pitch))) * Math.cos(Math.asin(accX));

       double heading = 180 * Math.atan2(magYcomp, magXcomp) / Math.PI;
       
       heading -= calibration.variation;

       if (heading < 0.0) heading += 360.0;
       
       return Utils.normalizeDegrees0_360(heading);
   }

   private double[] getCalibratedMag(double[] mag) {
       return new double[] {
               mag[0] - calibration.x,
               mag[1] - calibration.y,
               mag[2] - calibration.z
       };
   }


}
