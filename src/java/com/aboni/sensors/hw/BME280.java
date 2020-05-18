package com.aboni.sensors.hw;

import com.aboni.misc.Utils;
import com.aboni.sensors.I2CInterface;

import java.io.IOException;

public class BME280 implements Atmospheric {

    public static final int BME280_I2C_ADDRESS = 0x76;

    private I2CInterface device;

    private int digT1;
    private int digT2;
    private int digT3;

    private int digP1;
    private int digP2;
    private int digP3;
    private int digP4;
    private int digP5;
    private int digP6;
    private int digP7;
    private int digP8;
    private int digP9;

    private int digH1;
    private int digH2;
    private int digH3;
    private int digH4;
    private int digH5;
    private int digH6;

    private long lastRead;
    private double cTemp;
    private double pressure;
    private double humidity;
    private double standardSeaLevelPressure;

    public BME280(I2CInterface i2cDevice) throws IOException {
        device = i2cDevice;
        init();
        read();
        lastRead = 1;
    }

    private void init() throws IOException {
        // Read 24 bytes of data from address 0x88(136)
        byte[] b1 = new byte[24];
        device.read(0x88, b1, 0, 24);
        // Convert the data
        // temp coefficients
        digT1 = (b1[0] & 0xFF) + ((b1[1] & 0xFF) * 256);
        digT2 = (b1[2] & 0xFF) + ((b1[3] & 0xFF) * 256);
        if (digT2 > 32767) {
            digT2 -= 65536;
        }
        digT3 = (b1[4] & 0xFF) + ((b1[5] & 0xFF) * 256);
        if (digT3 > 32767) {
            digT3 -= 65536;
        }
        // pressure coefficients
        digP1 = (b1[6] & 0xFF) + ((b1[7] & 0xFF) * 256);
        digP2 = (b1[8] & 0xFF) + ((b1[9] & 0xFF) * 256);
        if (digP2 > 32767) {
            digP2 -= 65536;
        }
        digP3 = (b1[10] & 0xFF) + ((b1[11] & 0xFF) * 256);
        if (digP3 > 32767) {
            digP3 -= 65536;
        }
        digP4 = (b1[12] & 0xFF) + ((b1[13] & 0xFF) * 256);
        if (digP4 > 32767) {
            digP4 -= 65536;
        }
        digP5 = (b1[14] & 0xFF) + ((b1[15] & 0xFF) * 256);
        if (digP5 > 32767) {
            digP5 -= 65536;
        }
        digP6 = (b1[16] & 0xFF) + ((b1[17] & 0xFF) * 256);
        if (digP6 > 32767) {
            digP6 -= 65536;
        }
        digP7 = (b1[18] & 0xFF) + ((b1[19] & 0xFF) * 256);
        if (digP7 > 32767) {
            digP7 -= 65536;
        }
        digP8 = (b1[20] & 0xFF) + ((b1[21] & 0xFF) * 256);
        if (digP8 > 32767) {
            digP8 -= 65536;
        }
        digP9 = (b1[22] & 0xFF) + ((b1[23] & 0xFF) * 256);
        if (digP9 > 32767) {
            digP9 -= 65536;
        }

        // Read 1 byte of data from address 0xA1(161)
        digH1 = ((byte) device.readU8(0xA1) & 0xFF);

        // Read 7 bytes of data from address 0xE1(225)
        device.read(0xE1, b1, 0, 7);

        // Convert the data
        // humidity coefficients
        digH2 = (b1[0] & 0xFF) + (b1[1] * 256);
        if (digH2 > 32767) {
            digH2 -= 65536;
        }
        digH3 = b1[2] & 0xFF;
        digH4 = ((b1[3] & 0xFF) * 16) + (b1[4] & 0xF);
        if (digH4 > 32767) {
            digH4 -= 65536;
        }
        digH5 = ((b1[4] & 0xFF) / 16) + ((b1[5] & 0xFF) * 16);
        if (digH5 > 32767) {
            digH5 -= 65536;
        }
        digH6 = b1[6] & 0xFF;
        if (digH6 > 127) {
            digH6 -= 256;
        }

        // Select control humidity register
        // Humidity over sampling rate = 1
        device.write(0xF2, (byte) 0x01);
        // Select control measurement register
        // Normal mode, temp and pressure over sampling rate = 1
        device.write(0xF4, (byte) 0x27);
        // Select config register
        // Stand_by time = 1000 ms
        device.write(0xF5, (byte) 0xA0);
    }

    private boolean read() {
        try {
            long now = System.currentTimeMillis();
            if (Utils.isOlderThan(lastRead, now, 1000)) {

                // Read 8 bytes of data from address 0xF7(247)
                // pressure msb1, pressure msb, pressure lsb, temp msb1, temp msb, temp lsb, humidity lsb, humidity msb
                byte[] data = new byte[8];
                device.read(0xF7, data, 0, 8);

                // Convert pressure and temperature data to 19-bits
                long adcP = (((long) (data[0] & 0xFF) * 65536) + ((long) (data[1] & 0xFF) * 256) + (long) (data[2] & 0xF0)) / 16;
                long adcT = (((long) (data[3] & 0xFF) * 65536) + ((long) (data[4] & 0xFF) * 256) + (long) (data[5] & 0xF0)) / 16;
                // Convert the humidity data
                long adcH = ((long) (data[6] & 0xFF) * 256 + (long) (data[7] & 0xFF));

                // Temperature offset calculations
                double var1 = (((double) adcT) / 16384.0 - ((double) digT1) / 1024.0) * ((double) digT2);
                double var2 = ((((double) adcT) / 131072.0 - ((double) digT1) / 8192.0) *
                        (((double) adcT) / 131072.0 - ((double) digT1) / 8192.0)) * ((double) digT3);
                double tFine = (long) (var1 + var2);
                cTemp = (var1 + var2) / 5120.0;

                // Pressure offset calculations
                var1 = (tFine / 2.0) - 64000.0;
                var2 = var1 * var1 * ((double) digP6) / 32768.0;
                var2 = var2 + var1 * ((double) digP5) * 2.0;
                var2 = (var2 / 4.0) + (((double) digP4) * 65536.0);
                var1 = (((double) digP3) * var1 * var1 / 524288.0 + ((double) digP2) * var1) / 524288.0;
                var1 = (1.0 + var1 / 32768.0) * ((double) digP1);
                double p = 1048576.0 - (double) adcP;
                p = (p - (var2 / 4096.0)) * 6250.0 / var1;
                var1 = ((double) digP9) * p * p / 2147483648.0;
                var2 = p * ((double) digP8) / 32768.0;
                pressure = (p + (var1 + var2 + ((double) digP7)) / 16.0);

                // Humidity offset calculations
                double varH = tFine - 76800.0;
                varH = (adcH - (digH4 * 64.0 + digH5 / 16384.0 * varH)) * (digH2 / 65536.0 * (1.0 + digH6 / 67108864.0 * varH * (1.0 + digH3 / 67108864.0 * varH)));
                humidity = varH * (1.0 - digH1 * varH / 524288.0);
                if (humidity > 100.0) {
                    humidity = 100.0;
                } else if (humidity < 0.0) {
                    humidity = 0.0;
                }
                lastRead = now;
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public float readTemperature() {
        if (read())
            return (float) cTemp;
        else
            return 0.0f;
    }

    @Override
    public float readPressure() {
        if (read())
            return (float) pressure;
        else
            return 0.0f;
    }

    @Override
    public float readHumidity() {
        if (read())
            return (float) humidity;
        else
            return 0.0f;
    }

    @Override
    public void setStandardSeaLevelPressure(int standardSeaLevelPressure) {
        this.standardSeaLevelPressure = standardSeaLevelPressure;
    }

    @Override
    public double readAltitude() {
        double altitude;
        float p = readPressure();
        altitude = 44330.0 * (1.0 - Math.pow(p / standardSeaLevelPressure, 0.1903));
        return altitude;
    }
}