package com.aboni.sensors.hw;

import com.aboni.misc.Utils;
import com.aboni.sensors.I2CInterface;

import java.io.IOException;

/*
 * Pressure, Altitude, Temperature, Humidity
 * Adapted from https://github.com/adafruit/Adafruit_Python_BME280
 */
public class BME280 implements Atmospheric {
    // This next addresses is returned by "sudo i2cdetect -y 1", see above.
    public static final int BME280_I2CADDR = 0x76;

    // Operating Modes
    public static final int BME280_OSAMPLE_1 = 1;
    public static final int BME280_OSAMPLE_2 = 2;
    public static final int BME280_OSAMPLE_4 = 3;
    public static final int BME280_OSAMPLE_8 = 4;
    public static final int BME280_OSAMPLE_16 = 5;

    // BME280 Registers
    protected static final int[] BME280_REGISTER_DIG_T = new int[]{0x88, 0x8A, 0x8C};
    protected static final int[] BME280_REGISTER_DIG_P = new int[]{0x8E, 0x90, 0x92, 0x94, 0x96, 0x98, 0x9A, 0x9C, 0x9E};

    public static final int BME280_REGISTER_DIG_H1 = 0xA1;
    public static final int BME280_REGISTER_DIG_H2 = 0xE1;
    public static final int BME280_REGISTER_DIG_H3 = 0xE3;
    public static final int BME280_REGISTER_DIG_H4 = 0xE4;
    public static final int BME280_REGISTER_DIG_H5 = 0xE5;
    public static final int BME280_REGISTER_DIG_H6 = 0xE6;
    public static final int BME280_REGISTER_DIG_H7 = 0xE7;

    public static final int BME280_REGISTER_CHIPID = 0xD0;
    public static final int BME280_REGISTER_VERSION = 0xD1;
    public static final int BME280_REGISTER_SOFTRESET = 0xE0;

    public static final int BME280_REGISTER_CONTROL_HUM = 0xF2;
    public static final int BME280_REGISTER_CONTROL = 0xF4;
    public static final int BME280_REGISTER_CONFIG = 0xF5;
    public static final int BME280_REGISTER_PRESSURE_DATA = 0xF7;
    public static final int BME280_REGISTER_TEMP_DATA = 0xFA;
    public static final int BME280_REGISTER_HUMIDITY_DATA = 0xFD;

    private static class DigH {
        private int digH1 = 0;
        private int digH2 = 0;
        private int digH3 = 0;
        private int digH4 = 0;
        private int digH5 = 0;
        private int digH6 = 0;
    }

    private float tFine;
    private final DigH digH = new DigH();
    private final int[] digT = new int[3];
    private final int[] digP = new int[9];

    private final I2CInterface i2cdevice;

    public BME280(I2CInterface i2cdevice) throws IOException {
        this.i2cdevice = i2cdevice;
        readCalibrationData();
        this.i2cdevice.write(BME280_REGISTER_CONTROL, (byte) 0x3F);
        tFine = 0.0f;
    }

	private int readU8(int register) throws IOException
	{
		return i2cdevice.readU8(register);
	}

	private int readS8(int register) throws IOException
	{
		return i2cdevice.readS8(register);
	}

	private int readU16LE(int register) throws IOException
	{
		return i2cdevice.readU16LE(register);
	}

	private int readS16LE(int register) throws IOException
	{
		return i2cdevice.readS16LE(register);
	}

	public void readCalibrationData() throws IOException {
        // Reads the calibration data from the IC
        for (int j = 0; j < 9; j++) digT[j] = readU16LE(BME280_REGISTER_DIG_T[j]);
        for (int j = 0; j < 9; j++) digP[0] = readU16LE(BME280_REGISTER_DIG_P[j]);

        digH.digH1 = readU8(BME280_REGISTER_DIG_H1);
        digH.digH2 = readS16LE(BME280_REGISTER_DIG_H2);
        digH.digH3 = readU8(BME280_REGISTER_DIG_H3);
        digH.digH6 = readS8(BME280_REGISTER_DIG_H7);

        int h4 = readS8(BME280_REGISTER_DIG_H4);
        h4 = (h4 << 24) >> 20;
        digH.digH4 = h4 | (readU8(BME280_REGISTER_DIG_H5) & 0x0F);

        int h5 = readS8(BME280_REGISTER_DIG_H6);
        h5 = (h5 << 24) >> 20;
        digH.digH5 = h5 | (readU8(BME280_REGISTER_DIG_H5) >> 4 & 0x0F);
    }

	private int readRawTemp() throws IOException {
        // Reads the raw (uncompensated) temperature from the sensor
        int meas = BME280_OSAMPLE_8;
        i2cdevice.write(BME280_REGISTER_CONTROL_HUM, (byte) meas); // HUM ?
        meas = BME280_OSAMPLE_8 << 5 | BME280_OSAMPLE_8 << 2 | 1;
        i2cdevice.write(BME280_REGISTER_CONTROL, (byte) meas);

        double sleepTime = 0.00125 + 0.0023 * (1 << BME280_OSAMPLE_8);
        sleepTime = sleepTime + 0.0023 * (1 << BME280_OSAMPLE_8) + 0.000575;
        sleepTime = sleepTime + 0.0023 * (1 << BME280_OSAMPLE_8) + 0.000575;
        waitFor((int) (sleepTime * 1000));
        int msb = readU8(BME280_REGISTER_TEMP_DATA);
        int lsb = readU8(BME280_REGISTER_TEMP_DATA + 1);
        int xlsb = readU8(BME280_REGISTER_TEMP_DATA + 2);
        return ((msb << 16) | (lsb << 8) | xlsb) >> 4;
    }

	private int readRawPressure() throws IOException
	{
		// Reads the raw (uncompensated) pressure level from the sensor
		int msb  = readU8(BME280_REGISTER_PRESSURE_DATA);
		int lsb  = readU8(BME280_REGISTER_PRESSURE_DATA + 1);
		int xlsb = readU8(BME280_REGISTER_PRESSURE_DATA + 2);
		return ((msb << 16) | (lsb << 8) | xlsb) >> 4;
	}

	private int readRawHumidity() throws IOException
	{
		int msb = readU8(BME280_REGISTER_HUMIDITY_DATA);
		int lsb = readU8(BME280_REGISTER_HUMIDITY_DATA + 1);
		return (msb << 8) | lsb;
	}

	/* (non-Javadoc)
	 * @see com.aboni.sensors.hw.AtmoSensor#readTemperature()
	 */
	@Override
	public float readTemperature() 
	{
		try {
            // Gets the compensated temperature in degrees celcius
            float ut = readRawTemp();
            float var1;
            float var2;
            float temp;

            // Read raw temp before aligning it with the calibration values
            var1 = (ut / 16384.0f - digT[0] / 1024.0f) * (float) digT[1];
            var2 = ((ut / 131072.0f - digT[0] / 8192.0f) * (ut / 131072.0f - digT[0] / 8192.0f)) * (float) digT[2];
            tFine = (int) (var1 + var2);
            temp = (var1 + var2) / 5120.0f;
            return temp;
        } catch (Exception e) {
			return 0f;
		}
	}

	/* (non-Javadoc)
	 * @see com.aboni.sensors.hw.AtmoSensor#readPressure()
	 */
	@Override
	public float readPressure() 
	{
		try {
            // Gets the compensated pressure in pascal
            int adc = readRawPressure();
            float var1 = (tFine / 2.0f) - 64000.0f;
            float var2 = var1 * var1 * (digP[5] / 32768.0f);
            var2 = var2 + var1 * digP[4] * 2.0f;
            var2 = (var2 / 4.0f) + (digP[3] * 65536.0f);
            var1 = (digP[2] * var1 * var1 / 524288.0f + digP[1] * var1) / 524288.0f;
            var1 = (1.0f + var1 / 32768.0f) * digP[0];
            if (var1 == 0f)
                return 0f;
            float p = 1048576.0f - adc;
            p = ((p - var2 / 4096.0f) * 6250.0f) / var1;
            var1 = digP[8] * p * p / 2147483648.0f;
            var2 = p * digP[7] / 32768.0f;
            p = p + (var1 + var2 + digP[6]) / 16.0f;
            return p;
        } catch (Exception e) {
			return 0f;
		}
	}

	/* (non-Javadoc)
	 * @see com.aboni.sensors.hw.AtmoSensor#readHumidity()
	 */
	@Override
	public float readHumidity() 
	{
		try {
            int adc = readRawHumidity();
            float h = tFine - 76800.0f;
            h = (adc - (digH.digH4 * 64.0f + digH.digH5 / 16384.8f * h)) *
                    (digH.digH2 / 65536.0f * (1.0f + digH.digH6 / 67108864.0f * h * (1.0f + digH.digH3 / 67108864.0f * h)));
            h = h * (1.0f - digH.digH1 * h / 524288.0f);
            if (h > 100)
                h = 100;
            else if (h < 0)
                h = 0;
            return h;
        } catch (Exception e) {
			return 0f;
		}
	}

	private int standardSeaLevelPressure = 101325;

	@Override
	public void setStandardSeaLevelPressure(int standardSeaLevelPressure)
	{
		this.standardSeaLevelPressure = standardSeaLevelPressure;
	}

    @Override
    public double readAltitude() {
        // "Calculates the altitude in meters"
        double altitude;
        float pressure = readPressure();
        altitude = 44330.0 * (1.0 - Math.pow(pressure / standardSeaLevelPressure, 0.1903));
        return altitude;
    }

    protected static void waitFor(int howMuch) {
        Utils.pause(howMuch);
    }
}
