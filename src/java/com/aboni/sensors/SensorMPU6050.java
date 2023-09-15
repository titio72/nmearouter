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

import com.aboni.nmea.router.utils.Log;
import com.aboni.utils.LPFFilter;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import javax.inject.Inject;
import java.io.IOException;

public class SensorMPU6050 extends I2CSensor {

    public static class AccelScale {
        private AccelScale() {}

        public static final double ACCEL_SCALE_2_G = 16384.0;
        public static final double ACCEL_SCALE_4_G =  8092.0;
        public static final double ACCEL_SCALE_8_G =  4096.0;
        public static final double ACCEL_SCALE_16_G =  2048.0;
    }

    public static class GyroScale {
        private GyroScale() {}

        public static final double GYRO_SCALE_250  = 131.0;
        public static final double GYRO_SCALE_500  =  65.5;
        public static final double GYRO_SCALE_1000 =  32.8;
        public static final double GYRO_SCALE_2000 =  16.4;
    }

    private static final int  MPU6050_I2CADDR = 0x68;

    public static class PowerManagement {
        private PowerManagement() {}

        public static final int POWER_MGMT_ADDR_1 = 0x6b;
        public static final int POWER_MGMT_ADDR_2 = 0x6a;
        public static final byte POWER_MGMT_ON = 0x01;
        public static final byte POWER_MGMT_OFF = 0x00;
    }

    public static class BypassMode {
        private BypassMode() {}

        public static final int BYPASS_MODE_ADDR = 0x37;
        public static final byte BYPASS_MODE_ON = 0x02;
        public static final byte BYPASS_MODE_OFF = 0x00;
    }

    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;

    private static final double GYRO_SCALE = GyroScale.GYRO_SCALE_250;
    private static final double ACCELL_SCALE = AccelScale.ACCEL_SCALE_2_G;

    private double[] scaledAccel;
    private double[] scaledGyro;

    private I2CInterface device;

    @Inject
    public SensorMPU6050(Log log) {
        super(log);
    }

    @Override
    protected void initSensor(int bus) throws SensorException {
        try {
            device = new I2CInterface(bus, MPU6050_I2CADDR);
            device.write(BypassMode.BYPASS_MODE_ADDR, BypassMode.BYPASS_MODE_ON);
            device.write(PowerManagement.POWER_MGMT_ADDR_2, PowerManagement.POWER_MGMT_OFF);
            device.write(PowerManagement.POWER_MGMT_ADDR_1, PowerManagement.POWER_MGMT_OFF);
        } catch (IOException | UnsupportedBusNumberException e) {
            throw new SensorException("Error initializing MPU60050", e);
        }
    }

    private void readA() throws IOException {
        int aX = device.readWord(0x3b);
        int aY = device.readWord(0x3d);
        int aZ = device.readWord(0x3f);
        double[] scaledAccel1 = new double[] {
                aX / ACCELL_SCALE,
                aY / ACCELL_SCALE,
                aZ / ACCELL_SCALE
        };

        if (scaledAccel==null) {
            scaledAccel = scaledAccel1;
        } else {
            scaledAccel = new double[] {
                    LPFFilter.getLPFReading(getDefaultSmoothingAlpha(), scaledAccel[X], scaledAccel1[X]),
                    LPFFilter.getLPFReading(getDefaultSmoothingAlpha(), scaledAccel[Y], scaledAccel1[Y]),
                    LPFFilter.getLPFReading(getDefaultSmoothingAlpha(), scaledAccel[Z], scaledAccel1[Z])
            };
        }
    }

    private void readG() throws IOException {
        int gX = device.readWord(0x43);
        int gY = device.readWord(0x45);
        int gZ = device.readWord(0x47);
        double[] scaledGyro1 = new double[]{
                gX / GYRO_SCALE,
                gY / GYRO_SCALE,
                gZ / GYRO_SCALE
        };
        if (scaledGyro==null) {
            scaledGyro = scaledGyro1;
        } else {
            scaledGyro = new double[]{
                    LPFFilter.getLPFReading(getDefaultSmoothingAlpha(), scaledGyro[X], scaledGyro1[X]),
                    LPFFilter.getLPFReading(getDefaultSmoothingAlpha(), scaledGyro[Y], scaledGyro1[Y]),
                    LPFFilter.getLPFReading(getDefaultSmoothingAlpha(), scaledGyro[Z], scaledGyro1[Z])
            };
        }
    }

    @Override
    protected void readSensor() throws SensorException{
        try {
            readA();
            readG();
        } catch (IOException e) {
            throw new SensorException("Error reading gyro", e);
        }
    }

    public double[] readRawAccel() {
        return scaledAccel;
    }

    public double[] readAccel() throws SensorNotInitializedException {
        if (isInitialized()) {
            double aXScaled = scaledAccel[X];
            double aYScaled = scaledAccel[Y];
            double aZScaled = scaledAccel[Z];
            return new double[]{getXRotation(aXScaled, aYScaled, aZScaled),
                    getYRotation(aXScaled, aYScaled, aZScaled),
                    getZRotation(aXScaled, aYScaled, aZScaled)};
        } else {
            throw new SensorNotInitializedException("Error reading accelerometer: sensor not initialized");
        }
    }

    public double[] readRawGyro() {
        return scaledGyro;
    }

    public double[] readGyro() throws SensorNotInitializedException {
        if (isInitialized()) {
            double gXScaled = scaledGyro[X];
            double gYScaled = scaledGyro[Y];
            double gZScaled = scaledGyro[Z];
            return new double[]{getXRotation(gXScaled, gYScaled, gZScaled),
                    getYRotation(gXScaled, gYScaled, gZScaled),
                    getZRotation(gXScaled, gYScaled, gZScaled)};
        } else {
            throw new SensorNotInitializedException("Error reading gyro: sensor not initialized");
        }
    }

    public double[] readAccelDegrees() throws SensorNotInitializedException {
        double[] r = readAccel();
        return new double[]{
                Math.toDegrees(r[X]),
                Math.toDegrees(r[Y]),
                Math.toDegrees(r[Z])
        };
    }

    public double getPitch() throws SensorNotInitializedException {
        return readAccel()[X];
    }

    public double getRoll() throws SensorNotInitializedException {
        return readAccel()[Y];
    }

    public double getPitchDegrees() throws SensorNotInitializedException {
        return Math.toDegrees(readAccel()[X]);
    }

    public double getRollDegrees() throws SensorNotInitializedException {
        return Math.toDegrees(readAccel()[Y]);
    }

    protected static double dist(double a, double b) {
        return Math.sqrt((a*a) + (b*b));
    }

    protected static double getYRotation(double x, double y, double z) {
        return Math.atan2(x, dist(y,z));
    }

    protected static double getXRotation(double x, double y, double z) {
        return Math.atan2(y, dist(x,z));
    }

    protected static double getZRotation(double x, double y, double z) {
        return Math.atan2(z, dist(x,y));
    }

    @Override
    public String getSensorName() {
        return "MPU6050";
    }
}