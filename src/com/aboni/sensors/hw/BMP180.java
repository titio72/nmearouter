package com.aboni.sensors.hw;

import java.io.IOException;

import com.aboni.sensors.I2CInterface;

/*
 * Altitude, Pressure, Temperature
 */
public class BMP180 implements Atmo {

	private final static I2CInterface.Endianness BMP180_ENDIANNESS = I2CInterface.Endianness.BIG_ENDIAN;
	// This next addresses is returned by "sudo i2cdetect -y 1", see above.
	public final static int BMP180_ADDRESS = 0x77; 
	// Operating Modes
	private final static int BMP180_ULTRALOWPOWER     = 0;
	private final static int BMP180_STANDARD          = 1;
	private final static int BMP180_HIGHRES           = 2;
	private final static int BMP180_ULTRAHIGHRES      = 3;

	// BMP180 Registers
	private final static int BMP180_CAL_AC1           = 0xAA;  // R   Calibration data (16 bits)
	private final static int BMP180_CAL_AC2           = 0xAC;  // R   Calibration data (16 bits)
	private final static int BMP180_CAL_AC3           = 0xAE;  // R   Calibration data (16 bits)
	private final static int BMP180_CAL_AC4           = 0xB0;  // R   Calibration data (16 bits)
	private final static int BMP180_CAL_AC5           = 0xB2;  // R   Calibration data (16 bits)
	private final static int BMP180_CAL_AC6           = 0xB4;  // R   Calibration data (16 bits)
	private final static int BMP180_CAL_B1            = 0xB6;  // R   Calibration data (16 bits)
	private final static int BMP180_CAL_B2            = 0xB8;  // R   Calibration data (16 bits)
	private final static int BMP180_CAL_MB            = 0xBA;  // R   Calibration data (16 bits)
	private final static int BMP180_CAL_MC            = 0xBC;  // R   Calibration data (16 bits)
	private final static int BMP180_CAL_MD            = 0xBE;  // R   Calibration data (16 bits)

	private final static int BMP180_CONTROL           = 0xF4;
	private final static int BMP180_TEMPDATA          = 0xF6;
	private final static int BMP180_PRESSUREDATA      = 0xF6;
	private final static int BMP180_READTEMPCMD       = 0x2E;
	private final static int BMP180_READPRESSURECMD   = 0x34;

	private int cal_AC1 = 0;
	private int cal_AC2 = 0;
	private int cal_AC3 = 0;
	private int cal_AC4 = 0;
	private int cal_AC5 = 0;
	private int cal_AC6 = 0;
	private int cal_B1  = 0;
	private int cal_B2  = 0;
	@SuppressWarnings("unused")
	private int cal_MB  = 0;
	private int cal_MC  = 0;
	private int cal_MD  = 0;

	private int mode = BMP180_STANDARD;
	
	private I2CInterface i2cdevice;

	public BMP180(I2CInterface i2cdevice) throws IOException {
		this.i2cdevice = i2cdevice;
		readCalibrationData();
	}

	private int readU16(int register) throws IOException
	{
		return getI2CBus().readU16(register, BMP180_ENDIANNESS);
	}

	private int readS16(int register) throws IOException
	{
		return getI2CBus().readS16(register, BMP180_ENDIANNESS);
	}

	private void readCalibrationData() throws IOException
	{
		// Reads the calibration data from the IC
		cal_AC1 = readS16(BMP180_CAL_AC1);   // INT16
		cal_AC2 = readS16(BMP180_CAL_AC2);   // INT16
		cal_AC3 = readS16(BMP180_CAL_AC3);   // INT16
		cal_AC4 = readU16(BMP180_CAL_AC4);   // UINT16
		cal_AC5 = readU16(BMP180_CAL_AC5);   // UINT16
		cal_AC6 = readU16(BMP180_CAL_AC6);   // UINT16
		cal_B1 =  readS16(BMP180_CAL_B1);    // INT16
		cal_B2 =  readS16(BMP180_CAL_B2);    // INT16
		cal_MB =  readS16(BMP180_CAL_MB);    // INT16
		cal_MC =  readS16(BMP180_CAL_MC);    // INT16
		cal_MD =  readS16(BMP180_CAL_MD);    // INT16
	}

	private int readRawTemp() throws Exception
	{
		// Reads the raw (uncompensated) temperature from the sensor
		getI2CBus().write(BMP180_CONTROL, (byte)BMP180_READTEMPCMD);
		waitfor(5);  // Wait 5ms
		int raw = readU16(BMP180_TEMPDATA);
		return raw;
	}

	private int readRawPressure() throws Exception
	{
		// Reads the raw (uncompensated) pressure level from the sensor
		getI2CBus().write(BMP180_CONTROL, (byte)(BMP180_READPRESSURECMD + (this.mode << 6)));
		if (this.mode == BMP180_ULTRALOWPOWER)
			waitfor(5);
		else if (this.mode == BMP180_HIGHRES)
			waitfor(14);
		else if (this.mode == BMP180_ULTRAHIGHRES)
			waitfor(26);
		else
			waitfor(8);
		int msb  = getI2CBus().readU8(BMP180_PRESSUREDATA);
		int lsb  = getI2CBus().readU8(BMP180_PRESSUREDATA + 1);
		int xlsb = getI2CBus().readU8(BMP180_PRESSUREDATA + 2);
		int raw = ((msb << 16) + (lsb << 8) + xlsb) >> (8 - this.mode);
		return raw;
	}

	public float readTemperature() {
		try {
			// Gets the compensated temperature in degrees celsius
			int UT = 0;
			int X1 = 0;
			int X2 = 0;
			int B5 = 0;
			float temp = 0.0f;
	
			// Read raw temp before aligning it with the calibration values
			UT = readRawTemp();
			X1 = ((UT - this.cal_AC6) * this.cal_AC5) >> 15;
			X2 = (this.cal_MC << 11) / (X1 + this.cal_MD);
			B5 = X1 + X2;
			temp = ((B5 + 8) >> 4) / 10.0f;
			return temp;
		} catch (Exception e) {
			return 0.0f;
		}
	}

	public float readPressure() {
		try {
			// Gets the compensated pressure in pascal
			int UT = 0;
			int UP = 0;
			int B3 = 0;
			int B5 = 0;
			int B6 = 0;
			int X1 = 0;
			int X2 = 0;
			int X3 = 0;
			int p =  0;
			int B4 = 0;
			int B7 = 0;
	
			UT = this.readRawTemp();
			UP = this.readRawPressure();
	
			// You can use the datasheet values to test the conversion results
			// boolean dsValues = true;
			boolean dsValues = false;
	
			if (dsValues)
			{
				UT = 27898;
				UP = 23843;
				this.cal_AC6 = 23153;
				this.cal_AC5 = 32757;
				this.cal_MB = -32768;
				this.cal_MC = -8711;
				this.cal_MD = 2868;
				this.cal_B1 = 6190;
				this.cal_B2 = 4;
				this.cal_AC3 = -14383;
				this.cal_AC2 = -72;
				this.cal_AC1 = 408;
				this.cal_AC4 = 32741;
				this.mode = BMP180_ULTRALOWPOWER;
			}
			// True Temperature Calculations
			X1 = (int)((UT - this.cal_AC6) * this.cal_AC5) >> 15;
			X2 = (this.cal_MC << 11) / (X1 + this.cal_MD);
			B5 = X1 + X2;
			// Pressure Calculations
			B6 = B5 - 4000;
			X1 = (this.cal_B2 * (B6 * B6) >> 12) >> 11;
			X2 = (this.cal_AC2 * B6) >> 11;
			X3 = X1 + X2;
			B3 = (((this.cal_AC1 * 4 + X3) << this.mode) + 2) / 4;
			X1 = (this.cal_AC3 * B6) >> 13;
			X2 = (this.cal_B1 * ((B6 * B6) >> 12)) >> 16;
			X3 = ((X1 + X2) + 2) >> 2;
			B4 = (this.cal_AC4 * (X3 + 32768)) >> 15;
			B7 = (UP - B3) * (50000 >> this.mode);
			if (B7 < 0x80000000)
				p = (B7 * 2) / B4;
			else
				p = (B7 / B4) * 2;
	
			X1 = (p >> 8) * (p >> 8);
			X1 = (X1 * 3038) >> 16;
			X2 = (-7357 * p) >> 16;
			p = p + ((X1 + X2 + 3791) >> 4);
	
			return p;
		} catch (Exception e) {
			return 0.0f;
		}
	}

	protected static void waitfor(long howMuch)
	{
		try { Thread.sleep(howMuch); } catch (InterruptedException ie) { ie.printStackTrace(); }
	}

	private I2CInterface getI2CBus() {
		return i2cdevice;
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
	
	@Override
	public float readHumidity() {
		return 0;
	}

}