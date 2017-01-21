package com.aboni.sensors;

import java.io.IOException;

import com.aboni.utils.DataFilter;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class SensorMPU6050 extends I2CSensor {
    
    public static class AccelScale {
        public static double ACCEL_SCALE_2g  = 16384.0;
        public static double ACCEL_SCALE_4g  =  8092.0;
        public static double ACCEL_SCALE_8g  =  4096.0;
        public static double ACCEL_SCALE_16g =  2048.0;
    }
    
    public static class GyroScale {
        public static double GYRO_SCALE_250  = 131.0;
        public static double GYRO_SCALE_500  =  65.5;
        public static double GYRO_SCALE_1000 =  32.8;
        public static double GYRO_SCALE_2000 =  16.4;
    }
   
    private static final int  MPU6050_I2CADDR = 0x68;
    
    private static final int power_mgmt_addr_1 = 0x6b;
    private static final int power_mgmt_addr_2 = 0x6a;
    @SuppressWarnings("unused")
	private static final byte power_mgmt_on = 0x01;
    private static final byte power_mgmt_off = 0x00;
    
    private static final int bypass_mode_addr =  0x37;
    private static final byte bypass_mode_on  = 0x02; 
    @SuppressWarnings("unused")
	private static final byte bypass_mode_off = 0x00; 
    
    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;
    
    private double gyroScale = GyroScale.GYRO_SCALE_250;
    private double accellScale = AccelScale.ACCEL_SCALE_2g;
    
    private double[] scaledAccel;
    private double[] scaledGyro;
    
    private I2CInterface device;
    
    public SensorMPU6050() {
        super();
    }
    
    @Override
    protected void _init(int bus) throws IOException, UnsupportedBusNumberException {
        device = new I2CInterface(bus, MPU6050_I2CADDR);
        device.write(bypass_mode_addr, bypass_mode_on);
        device.write(power_mgmt_addr_2, power_mgmt_off);
        device.write(power_mgmt_addr_1, power_mgmt_off);

    }
   
    void _readA() throws IOException {
        int a_x = device.readWord(0x3b);
        int a_y = device.readWord(0x3d);
        int a_z = device.readWord(0x3f);
        double[] scaledAccel1 = new double[] {
                (double)a_x / accellScale,
                (double)a_y / accellScale,
                (double)a_z / accellScale
        };

        if (scaledAccel==null) {
            scaledAccel = scaledAccel1;
        } else {
            scaledAccel = new double[] {
                    DataFilter.getLPFReading(getDefaultSmootingAlpha(), scaledAccel[X], scaledAccel1[X]),
                    DataFilter.getLPFReading(getDefaultSmootingAlpha(), scaledAccel[Y], scaledAccel1[Y]),
                    DataFilter.getLPFReading(getDefaultSmootingAlpha(), scaledAccel[Z], scaledAccel1[Z])
            };
        }
    }

    void _readG() throws IOException {
        int g_x = device.readWord(0x43);
        int g_y = device.readWord(0x45);
        int g_z = device.readWord(0x47);
        double[] scaledGyro1 = new double[] {
                (double)g_x / gyroScale,
                (double)g_y / gyroScale,
                (double)g_z / gyroScale
        };
        if (scaledGyro==null) {
            scaledGyro = scaledGyro1;
        } else {
            scaledGyro = new double[] {
                    DataFilter.getLPFReading(getDefaultSmootingAlpha(), scaledGyro[X], scaledGyro1[X]),
                    DataFilter.getLPFReading(getDefaultSmootingAlpha(), scaledGyro[Y], scaledGyro1[Y]),
                    DataFilter.getLPFReading(getDefaultSmootingAlpha(), scaledGyro[Z], scaledGyro1[Z])
            };
        }
    }
    
    @Override
    protected void _read() throws Exception {
        _readA();
        _readG();
    }
    
    public double[] readRawAccel() {
        return scaledAccel;
    }
    
    public double[] readAccel() throws SensorNotInititalizedException {
        if (isInitialized()) {
            double a_x_scaled = scaledAccel[X];
            double a_y_scaled = scaledAccel[Y];
            double a_z_scaled = scaledAccel[Z];
            return new double[] {getXRotation(a_x_scaled, a_y_scaled, a_z_scaled),
                    getYRotation(a_x_scaled, a_y_scaled, a_z_scaled),
                    getZRotation(a_x_scaled, a_y_scaled, a_z_scaled)};
        } else {
            throw new SensorNotInititalizedException("Error reading accelerometer: sensor not initialized");
        }
    }
    
    public double[] readRawGyro() {
        return scaledGyro;
    }
    
    public double[] readGyro() throws SensorNotInititalizedException {
        if (isInitialized()) {
            double g_x_scaled = scaledGyro[X];
            double g_y_scaled = scaledGyro[Y];
            double g_z_scaled = scaledGyro[Z];
            return new double[] {getXRotation(g_x_scaled, g_y_scaled, g_z_scaled),
                    getYRotation(g_x_scaled, g_y_scaled, g_z_scaled),
                    getZRotation(g_x_scaled, g_y_scaled, g_z_scaled)};
        } else {
            throw new SensorNotInititalizedException("Error reading gyro: sensor not initialized");
        }
    }
    
    public double[] readAccelDegrees() throws SensorNotInititalizedException {
        double[] r = readAccel();
        return new double[] {
                Math.toDegrees(r[X]), 
                Math.toDegrees(r[Y]), 
                Math.toDegrees(r[Z])
        };
    }
    
    public double getPitch() throws SensorNotInititalizedException {
        return readAccel()[X];
    }
    
    public double getRoll() throws SensorNotInititalizedException {
        return readAccel()[Y];
    }

    public double getPitchDegrees() throws SensorNotInititalizedException {
        return Math.toDegrees(readAccel()[X]);
    }
    
    public double getRollDegrees() throws SensorNotInititalizedException {
        return Math.toDegrees(readAccel()[Y]);
    }

    
    protected static double dist(double x, double y) {
        return Math.sqrt((x*x) + (y*y));
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