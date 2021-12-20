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

package com.aboni.sensors.hw;

import com.aboni.misc.Utils;
import com.aboni.sensors.I2CInterface;
import com.aboni.utils.Log;

import java.io.IOException;

public class BME280 implements Atmospheric {

    public static final int BME280_I2C_ADDRESS = 0x76;

    private final I2CInterface device;

    private int[] digT;
    private int[] digP;
    private int[] digH;
    private long lastRead;
    private double cTemp;
    private double pressure;
    private double humidity;
    private double standardSeaLevelPressure;
    private final byte[] dataBuffer = new byte[8];

    public BME280(I2CInterface i2cDevice) throws IOException {
        device = i2cDevice;
        init();
        read();
    }

    private static int signed16Bits(byte[] data, int offset) {
        int byte0 = data[offset] & 0xff;
        int byte1 = data[offset + 1];

        return (byte1 << 8) + byte0;
    }

    private static int unsigned16Bits(byte[] data, int offset) {
        int byte0 = data[offset] & 0xff;
        int byte1 = data[offset + 1] & 0xff;

        return (byte1 << 8) + byte0;
    }

    private void init() throws IOException {
        // Read 24 bytes of data from address 0x88(136)
        byte[] data = new byte[24];
        device.read(0x88, data, 0, 24);
        // Convert the data
        // temp coefficients
        digT = new int[]{
                unsigned16Bits(data, 0),
                signed16Bits(data, 2),
                signed16Bits(data, 4)
        };

        // pressure coefficients
        digP = new int[]{
                unsigned16Bits(data, 6),
                signed16Bits(data, 8),
                signed16Bits(data, 10),
                signed16Bits(data, 12),
                signed16Bits(data, 14),
                signed16Bits(data, 16),
                signed16Bits(data, 18),
                signed16Bits(data, 20),
                signed16Bits(data, 22)
        };

        // Read 7 bytes of data from address 0xE1(225)
        device.read(0xE1, data, 0, 7);
        // humidity coefficients
        digH = new int[]{
                // Read 1 byte of data from address 0xA1(161)
                device.readU8(0xA1) & 0xFF,
                signed16Bits(data, 0),
                data[2] & 0xFF,
                ((data[3] & 0xff) << 4) + (data[4] & 0x0f),
                ((data[5] & 0xff) << 4) + ((data[4] & 0xff) >> 4),
                data[6]
        };

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

    public void printParameters(Log logger, String logPrefix) {
        logger.debug(String.format("%s dig_T1:{%d} u16", logPrefix, digT[0]));
        logger.debug(String.format("%s dig_T2:{%d} s16", logPrefix, digT[1]));
        logger.debug(String.format("%s dig_T3:{%d} s16", logPrefix, digT[2]));

        logger.debug(String.format("%s dig_P1:{%d} u16", logPrefix, digP[0]));
        logger.debug(String.format("%s dig_P2:{%d} s16", logPrefix, digP[1]));
        logger.debug(String.format("%s dig_P3:{%d} s16", logPrefix, digP[2]));
        logger.debug(String.format("%s dig_P4:{%d} s16", logPrefix, digP[3]));
        logger.debug(String.format("%s dig_P5:{%d} s16", logPrefix, digP[4]));
        logger.debug(String.format("%s dig_P6:{%d} s16", logPrefix, digP[5]));
        logger.debug(String.format("%s dig_P7:{%d} s16", logPrefix, digP[6]));
        logger.debug(String.format("%s dig_P8:{%d} s16", logPrefix, digP[7]));
        logger.debug(String.format("%s dig_P9:{%d} s16", logPrefix, digP[8]));

        logger.debug(String.format("%s dig_H1:{%d} u8", logPrefix, digH[0]));
        logger.debug(String.format("%s dig_H2:{%d} s16", logPrefix, digH[1]));
        logger.debug(String.format("%s dig_H3:{%d} u8", logPrefix, digH[2]));
        logger.debug(String.format("%s dig_H4:{%d} s16", logPrefix, digH[3]));
        logger.debug(String.format("%s dig_H5:{%d} s16", logPrefix, digH[4]));
        logger.debug(String.format("%s dig_H6:{%d} s8", logPrefix, digH[5]));
    }

    private boolean read() {
        try {
            long now = System.currentTimeMillis();
            if (Utils.isOlderThan(lastRead, now, 1000)) {

                // Read 8 bytes of data from address 0xF7(247)
                // pressure msb1, pressure msb, pressure lsb, temp msb1, temp msb, temp lsb, humidity lsb, humidity msb
                device.read(0xF7, dataBuffer, 0, 8);

                // Convert pressure and temperature data to 19-bits
                int adcP = (((dataBuffer[0] & 0xff) << 16) + ((dataBuffer[1] & 0xff) << 8) + (dataBuffer[2] & 0xff)) >> 4;
                int adcT = (((dataBuffer[3] & 0xff) << 16) + ((dataBuffer[4] & 0xff) << 8) + (dataBuffer[5] & 0xff)) >> 4;
                int adcH = ((dataBuffer[6] & 0xff) << 8) + (dataBuffer[7] & 0xff);

                // Temperature offset calculations
                double var1 = (adcT / 16384.0 - digT[0] / 1024.0) * digT[1];
                double var2 = ((adcT / 131072.0 - digT[0] / 8192.0) *
                        (adcT / 131072.0 - digT[0] / 8192.0)) * digT[2];
                double tFine = (long) (var1 + var2);
                cTemp = (var1 + var2) / 5120.0;

                // Pressure offset calculations
                var1 = (tFine / 2.0) - 64000.0;
                var2 = var1 * var1 * digP[5] / 32768.0;
                var2 = var2 + var1 * digP[4] * 2.0;
                var2 = (var2 / 4.0) + (digP[3] * 65536.0);
                var1 = (digP[2] * var1 * var1 / 524288.0 + digP[1] * var1) / 524288.0;
                var1 = (1.0 + var1 / 32768.0) * digP[0];
                double p = 1048576.0 - adcP;
                p = (p - (var2 / 4096.0)) * 6250.0 / var1;
                var1 = digP[8] * p * p / 2147483648.0;
                var2 = p * digP[7] / 32768.0;
                pressure = (p + (var1 + var2 + digP[6]) / 16.0);

                // Humidity offset calculations
                double varH = tFine - 76800.0;
                varH = (adcH - (digH[3] * 64.0 + digH[4] / 16384.0 * varH)) *
                        (digH[1] / 65536.0 * (1.0 + digH[5] / 67108864.0 * varH * (1.0 + digH[2] / 67108864.0 * varH)));
                humidity = varH * (1.0 - digH[0] * varH / 524288.0);
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