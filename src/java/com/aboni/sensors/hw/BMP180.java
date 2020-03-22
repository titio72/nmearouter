package com.aboni.sensors.hw;

import com.aboni.misc.Utils;
import com.aboni.sensors.I2CInterface;

import java.io.IOException;

/*
 * Altitude, Pressure, Temperature
 */
public class BMP180 implements Atmospheric {

    private static final I2CInterface.Endianness BMP180_ENDIANNESS = I2CInterface.Endianness.BIG_ENDIAN;
    // This next addresses is returned by "sudo i2cdetect -y 1", see above.
    public static final int BMP180_ADDRESS = 0x77;
    // Operating Modes
    private static final int BMP180_ULTRALOWPOWER = 0;
    private static final int BMP180_STANDARD = 1;
    private static final int BMP180_HIGHRES = 2;
    private static final int BMP180_ULTRAHIGHRES = 3;

	// BMP180 Registers
	private static final int BMP180_CAL_AC1           = 0xAA;  // R   Calibration data (16 bits)
	private static final int BMP180_CAL_AC2           = 0xAC;  // R   Calibration data (16 bits)
	private static final int BMP180_CAL_AC3           = 0xAE;  // R   Calibration data (16 bits)
	private static final int BMP180_CAL_AC4           = 0xB0;  // R   Calibration data (16 bits)
	private static final int BMP180_CAL_AC5           = 0xB2;  // R   Calibration data (16 bits)
	private static final int BMP180_CAL_AC6           = 0xB4;  // R   Calibration data (16 bits)
	private static final int BMP180_CAL_B1            = 0xB6;  // R   Calibration data (16 bits)
	private static final int BMP180_CAL_B2            = 0xB8;  // R   Calibration data (16 bits)
	private static final int BMP180_CAL_MB            = 0xBA;  // R   Calibration data (16 bits)
	private static final int BMP180_CAL_MC            = 0xBC;  // R   Calibration data (16 bits)
	private static final int BMP180_CAL_MD            = 0xBE;  // R   Calibration data (16 bits)

	private static final int BMP180_CONTROL           = 0xF4;
	private static final int BMP180_TEMPDATA          = 0xF6;
	private static final int BMP180_PRESSUREDATA      = 0xF6;
	private static final int BMP180_READTEMPCMD       = 0x2E;
	private static final int BMP180_READPRESSURECMD   = 0x34;

	private int calAC1 = 0;
	private int calAC2 = 0;
	private int calAC3 = 0;
	private int calAC4 = 0;
	private int calAC5 = 0;
	private int calAC6 = 0;
	private int calB1 = 0;
	private int calB2 = 0;
	@SuppressWarnings({"unused", "FieldCanBeLocal"})
	private int calMB = 0;
	private int calMC = 0;
	private int calMD = 0;

	private final I2CInterface i2cdevice;

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
		calAC1 = readS16(BMP180_CAL_AC1);   // INT16
		calAC2 = readS16(BMP180_CAL_AC2);   // INT16
		calAC3 = readS16(BMP180_CAL_AC3);   // INT16
		calAC4 = readU16(BMP180_CAL_AC4);   // UINT16
		calAC5 = readU16(BMP180_CAL_AC5);   // UINT16
		calAC6 = readU16(BMP180_CAL_AC6);   // UINT16
		calB1 =  readS16(BMP180_CAL_B1);    // INT16
		calB2 =  readS16(BMP180_CAL_B2);    // INT16
		calMB =  readS16(BMP180_CAL_MB);    // INT16
		calMC =  readS16(BMP180_CAL_MC);    // INT16
		calMD =  readS16(BMP180_CAL_MD);    // INT16
	}

	private int readRawTemp() throws IOException
	{
		// Reads the raw (uncompensated) temperature from the sensor
		getI2CBus().write(BMP180_CONTROL, (byte)BMP180_READTEMPCMD);
		waitfor(5);  // Wait 5ms
		return readU16(BMP180_TEMPDATA);
	}

	private int getMode() {
		return BMP180_STANDARD;
	}

	private int readRawPressure() throws IOException
	{
		// Reads the raw (uncompensated) pressure level from the sensor
		getI2CBus().write(BMP180_CONTROL, (byte)(BMP180_READPRESSURECMD + (BMP180_STANDARD << 6)));

		switch (getMode()) {
			case BMP180_ULTRALOWPOWER:
				waitfor(5);
				break;
			case BMP180_HIGHRES:
				waitfor(14);
				break;
			case BMP180_ULTRAHIGHRES:
				waitfor(26); break;
			default:
				waitfor(8); break;
		}
		int msb  = getI2CBus().readU8(BMP180_PRESSUREDATA);
		int lsb  = getI2CBus().readU8(BMP180_PRESSUREDATA + 1);
		int xlsb = getI2CBus().readU8(BMP180_PRESSUREDATA + 2);
		return ((msb << 16) + (lsb << 8) + xlsb) >> (8 - BMP180_STANDARD);
	}

	public float readTemperature() {
		try {
			// Gets the compensated temperature in degrees celsius
			int iUT;
			int iX1;
			int iX2;
			int iB5;
			float temp;
	
			// Read raw temp before aligning it with the calibration values
			iUT = readRawTemp();
			iX1 = ((iUT - this.calAC6) * this.calAC5) >> 15;
			iX2 = (this.calMC << 11) / (iX1 + this.calMD);
			iB5 = iX1 + iX2;
			temp = ((iB5 + 8) >> 4) / 10.0f;
			return temp;
		} catch (Exception e) {
			return 0.0f;
		}
	}

	public float readPressure() {
		try {
			// Gets the compensated pressure in pascal
			int iUT;
			int iUP;
			int iB3;
			int iB5;
			int iB6;
			int iX1;
			int iX2;
			int iX3;
			int p;
			int iB4;
			int iB7;
	
			iUT = this.readRawTemp();
			iUP = this.readRawPressure();
	
			// True Temperature Calculations
			iX1 = ((iUT - this.calAC6) * this.calAC5) >> 15;
			iX2 = (this.calMC << 11) / (iX1 + this.calMD);
			iB5 = iX1 + iX2;
			// Pressure Calculations
			iB6 = iB5 - 4000;
			iX1 = (this.calB2 * (iB6 * iB6) >> 12) >> 11;
			iX2 = (this.calAC2 * iB6) >> 11;
			iX3 = iX1 + iX2;
			iB3 = (((this.calAC1 * 4 + iX3) << BMP180_STANDARD) + 2) / 4;
			iX1 = (this.calAC3 * iB6) >> 13;
			iX2 = (this.calB1 * ((iB6 * iB6) >> 12)) >> 16;
			iX3 = ((iX1 + iX2) + 2) >> 2;
			iB4 = (this.calAC4 * (iX3 + 32768)) >> 15;
			iB7 = (iUP - iB3) * (50000 >> BMP180_STANDARD);
			p = (iB7 / iB4) * 2;
	
			iX1 = (p >> 8) * (p >> 8);
			iX1 = (iX1 * 3038) >> 16;
			iX2 = (-7357 * p) >> 16;
			p = p + ((iX1 + iX2 + 3791) >> 4);
	
			return p;
		} catch (Exception e) {
			return 0.0f;
		}
	}

	private static void waitfor(int howMuch)
	{
		Utils.pause(howMuch);
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
		double altitude;
		float pressure = readPressure();
		altitude = 44330.0 * (1.0 - Math.pow(pressure / standardSeaLevelPressure, 0.1903));
		return altitude;
	}
	
	@Override
	public float readHumidity() {
		return 0;
	}

}