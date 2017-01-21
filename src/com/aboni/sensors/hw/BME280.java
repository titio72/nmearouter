package com.aboni.sensors.hw;

import com.aboni.sensors.I2CInterface;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/*
 * Pressure, Altitude, Temperature, Humidity
 * Adapted from https://github.com/adafruit/Adafruit_Python_BME280
 */
public class BME280 implements Atmo
{
	// This next addresses is returned by "sudo i2cdetect -y 1", see above.
	public final static int BME280_I2CADDR = 0x76;

	// Operating Modes
	public final static int BME280_OSAMPLE_1  = 1;
	public final static int BME280_OSAMPLE_2  = 2;
	public final static int BME280_OSAMPLE_4  = 3;
	public final static int BME280_OSAMPLE_8  = 4;
	public final static int BME280_OSAMPLE_16 = 5;

	// BME280 Registers
	public final static int BME280_REGISTER_DIG_T1 = 0x88;  // Trimming parameter registers
	public final static int BME280_REGISTER_DIG_T2 = 0x8A;
	public final static int BME280_REGISTER_DIG_T3 = 0x8C;

	public final static int BME280_REGISTER_DIG_P1 = 0x8E;
	public final static int BME280_REGISTER_DIG_P2 = 0x90;
	public final static int BME280_REGISTER_DIG_P3 = 0x92;
	public final static int BME280_REGISTER_DIG_P4 = 0x94;
	public final static int BME280_REGISTER_DIG_P5 = 0x96;
	public final static int BME280_REGISTER_DIG_P6 = 0x98;
	public final static int BME280_REGISTER_DIG_P7 = 0x9A;
	public final static int BME280_REGISTER_DIG_P8 = 0x9C;
	public final static int BME280_REGISTER_DIG_P9 = 0x9E;

	public final static int BME280_REGISTER_DIG_H1 = 0xA1;
	public final static int BME280_REGISTER_DIG_H2 = 0xE1;
	public final static int BME280_REGISTER_DIG_H3 = 0xE3;
	public final static int BME280_REGISTER_DIG_H4 = 0xE4;
	public final static int BME280_REGISTER_DIG_H5 = 0xE5;
	public final static int BME280_REGISTER_DIG_H6 = 0xE6;
	public final static int BME280_REGISTER_DIG_H7 = 0xE7;

	public final static int BME280_REGISTER_CHIPID    = 0xD0;
	public final static int BME280_REGISTER_VERSION   = 0xD1;
	public final static int BME280_REGISTER_SOFTRESET = 0xE0;

	public final static int BME280_REGISTER_CONTROL_HUM   = 0xF2;
	public final static int BME280_REGISTER_CONTROL       = 0xF4;
	public final static int BME280_REGISTER_CONFIG        = 0xF5;
	public final static int BME280_REGISTER_PRESSURE_DATA = 0xF7;
	public final static int BME280_REGISTER_TEMP_DATA     = 0xFA;
	public final static int BME280_REGISTER_HUMIDITY_DATA = 0xFD;

	private int dig_T1 = 0;
	private int dig_T2 = 0;
	private int dig_T3 = 0;

	private int dig_P1 = 0;
	private int dig_P2 = 0;
	private int dig_P3 = 0;
	private int dig_P4 = 0;
	private int dig_P5 = 0;
	private int dig_P6 = 0;
	private int dig_P7 = 0;
	private int dig_P8 = 0;
	private int dig_P9 = 0;

	private int dig_H1 = 0;
	private int dig_H2 = 0;
	private int dig_H3 = 0;
	private int dig_H4 = 0;
	private int dig_H5 = 0;
	private int dig_H6 = 0;

	private float tFine = 0F;

	private int mode = BME280_OSAMPLE_8;

	private I2CInterface i2cdevice;

	public BME280(I2CInterface i2cdevice) throws UnsupportedBusNumberException, IOException
	{
		this.i2cdevice = i2cdevice;
		try { this.readCalibrationData(); }
		catch (Exception ex)
		{ ex.printStackTrace(); }   
		this.i2cdevice.write(BME280_REGISTER_CONTROL, (byte)0x3F);
		tFine = 0.0f;
	}

	private int readU8(int register) throws Exception
	{
		return i2cdevice.readU8(register);
	}

	private int readS8(int register) throws Exception
	{
		return i2cdevice.readS8(register);
	}

	private int readU16LE(int register) throws Exception
	{
		return i2cdevice.readU16LE(register);
	}

	private int readS16LE(int register) throws Exception
	{
		return i2cdevice.readS16LE(register);
	}

	public void readCalibrationData() throws Exception
	{
		// Reads the calibration data from the IC
		dig_T1 = readU16LE(BME280_REGISTER_DIG_T1);
		dig_T2 = readS16LE(BME280_REGISTER_DIG_T2);
		dig_T3 = readS16LE(BME280_REGISTER_DIG_T3);

		dig_P1 = readU16LE(BME280_REGISTER_DIG_P1);
		dig_P2 = readS16LE(BME280_REGISTER_DIG_P2);
		dig_P3 = readS16LE(BME280_REGISTER_DIG_P3);
		dig_P4 = readS16LE(BME280_REGISTER_DIG_P4);
		dig_P5 = readS16LE(BME280_REGISTER_DIG_P5);
		dig_P6 = readS16LE(BME280_REGISTER_DIG_P6);
		dig_P7 = readS16LE(BME280_REGISTER_DIG_P7);
		dig_P8 = readS16LE(BME280_REGISTER_DIG_P8);
		dig_P9 = readS16LE(BME280_REGISTER_DIG_P9);

		dig_H1 = readU8(BME280_REGISTER_DIG_H1);
		dig_H2 = readS16LE(BME280_REGISTER_DIG_H2);
		dig_H3 = readU8(BME280_REGISTER_DIG_H3);
		dig_H6 = readS8(BME280_REGISTER_DIG_H7);

		int h4 = readS8(BME280_REGISTER_DIG_H4);
		h4 = (h4 << 24) >> 20;
		dig_H4 = h4 | (readU8(BME280_REGISTER_DIG_H5) & 0x0F);

		int h5 = readS8(BME280_REGISTER_DIG_H6);
		h5 = (h5 << 24) >> 20;
		dig_H5 = h5 | (readU8(BME280_REGISTER_DIG_H5) >> 4 & 0x0F);
	}

	private int readRawTemp() throws Exception
	{
		// Reads the raw (uncompensated) temperature from the sensor
		int meas = mode;
		i2cdevice.write(BME280_REGISTER_CONTROL_HUM, (byte)meas); // HUM ?
		meas = mode << 5 | mode << 2 | 1;
		i2cdevice.write(BME280_REGISTER_CONTROL, (byte)meas);

		double sleepTime = 0.00125 + 0.0023 * (1 << mode);
		sleepTime = sleepTime + 0.0023 * (1 << mode) + 0.000575;
		sleepTime = sleepTime + 0.0023 * (1 << mode) + 0.000575;
		waitfor((long)(sleepTime * 1000L));
		int msb  = readU8(BME280_REGISTER_TEMP_DATA);
		int lsb  = readU8(BME280_REGISTER_TEMP_DATA + 1);
		int xlsb = readU8(BME280_REGISTER_TEMP_DATA + 2);
		int raw  = ((msb << 16) | (lsb << 8) | xlsb) >> 4;
		return raw;
	}

	private int readRawPressure() throws Exception
	{
		// Reads the raw (uncompensated) pressure level from the sensor
		int msb  = readU8(BME280_REGISTER_PRESSURE_DATA);
		int lsb  = readU8(BME280_REGISTER_PRESSURE_DATA + 1);
		int xlsb = readU8(BME280_REGISTER_PRESSURE_DATA + 2);
		int raw = ((msb << 16) | (lsb << 8) | xlsb) >> 4;
		return raw;
	}

	private int readRawHumidity() throws Exception
	{
		int msb = readU8(BME280_REGISTER_HUMIDITY_DATA);
		int lsb = readU8(BME280_REGISTER_HUMIDITY_DATA + 1);
		int raw = (msb << 8) | lsb;
		return raw;
	}

	/* (non-Javadoc)
	 * @see com.aboni.sensors.hw.AtmoSensor#readTemperature()
	 */
	@Override
	public float readTemperature() 
	{
		try {
			// Gets the compensated temperature in degrees celcius
			float UT = readRawTemp();
			float var1 = 0;
			float var2 = 0;
			float temp = 0.0f;
	
			// Read raw temp before aligning it with the calibration values
			var1 = (UT / 16384.0f - dig_T1 / 1024.0f) * (float)dig_T2;
			var2 = ((UT / 131072.0f - dig_T1 / 8192.0f) * (UT / 131072.0f - dig_T1 / 8192.0f)) * (float)dig_T3;
			tFine = (int)(var1 + var2);
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
			float var2 = var1 * var1 * (dig_P6 / 32768.0f);
			var2 = var2 + var1 * dig_P5 * 2.0f;
			var2 = (var2 / 4.0f) + (dig_P4 * 65536.0f);
			var1 = (dig_P3 * var1 * var1 / 524288.0f + dig_P2 * var1) / 524288.0f;
			var1 = (1.0f + var1 / 32768.0f) * dig_P1;
			if (var1 == 0f)
				return 0f;
			float p = 1048576.0f - adc;
			p = ((p - var2 / 4096.0f) * 6250.0f) / var1;
			var1 = dig_P9 * p * p / 2147483648.0f;
			var2 = p * dig_P8 / 32768.0f;
			p = p + (var1 + var2 + dig_P7) / 16.0f;
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
			h = (adc - (dig_H4 * 64.0f + dig_H5 / 16384.8f * h)) * 
					(dig_H2 / 65536.0f * (1.0f + dig_H6 / 67108864.0f * h * (1.0f + dig_H3 / 67108864.0f * h)));
			h = h * (1.0f - dig_H1 * h / 524288.0f);
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

	/* (non-Javadoc)
	 * @see com.aboni.sensors.hw.AtmoSensor#setStandardSeaLevelPressure(int)
	 */
	@Override
	public void setStandardSeaLevelPressure(int standardSeaLevelPressure)
	{
		this.standardSeaLevelPressure = standardSeaLevelPressure;
	}

	/* (non-Javadoc)
	 * @see com.aboni.sensors.hw.AtmoSensor#readAltitude()
	 */
	@Override
	public double readAltitude() 
	{
		// "Calculates the altitude in meters"
		double altitude = 0.0;
		float pressure = readPressure();
		altitude = 44330.0 * (1.0 - Math.pow(pressure / standardSeaLevelPressure, 0.1903));
		return altitude;
	}

	protected static void waitfor(long howMuch)
	{
		try { Thread.sleep(howMuch); } catch (InterruptedException ie) { ie.printStackTrace(); }
	}

	public static void main(String[] args) throws UnsupportedBusNumberException, IOException
	{
		final NumberFormat NF = new DecimalFormat("##00.00");
		I2CInterface iff = new I2CInterface(1, BME280.BME280_I2CADDR); 
		Atmo sensor = new BME280(iff);
		float press = 0;
		float temp  = 0;
		float hum   = 0;
		double alt  = 0;

		try { temp = sensor.readTemperature(); } 
		catch (Exception ex) 
		{ 
			System.err.println(ex.getMessage()); 
			ex.printStackTrace();
		}
		try { press = sensor.readPressure(); } 
		catch (Exception ex) 
		{ 
			System.err.println(ex.getMessage()); 
			ex.printStackTrace();
		}
		sensor.setStandardSeaLevelPressure((int)press); // As we ARE at the sea level (in San Francisco).
		try { alt = sensor.readAltitude(); } 
		catch (Exception ex) 
		{ 
			System.err.println(ex.getMessage()); 
			ex.printStackTrace();
		}

		try { hum = sensor.readHumidity(); } 
		catch (Exception ex) 
		{ 
			System.err.println(ex.getMessage()); 
			ex.printStackTrace();
		}

		System.out.println("Temperature: " + NF.format(temp) + " C");
		System.out.println("Pressure   : " + NF.format(press / 100) + " hPa");
		System.out.println("Altitude   : " + NF.format(alt) + " m");
		System.out.println("Humidity   : " + NF.format(hum) + " %");
	}
}
