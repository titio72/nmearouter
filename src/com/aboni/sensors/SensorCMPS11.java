package com.aboni.sensors;

import java.io.IOException;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class SensorCMPS11 extends ASensorCompass {

    private I2CInterface device;
	
	public SensorCMPS11() {
		super();
	}
	
	private final int ADDR = 0x60;
	private int h255;
	private double heading;
	private double pitch;
	private double roll;
	
	@Override
	protected void _init(int bus) throws IOException, UnsupportedBusNumberException {
        device = new I2CInterface(bus, ADDR);
	}

	public int getHeading255() {
		return h255;
	}
	
	@Override
	public double getUnfilteredSensorHeading() throws SensorNotInititalizedException {
		return heading;
	}

	@Override
	public double getUnfilteredPitch() throws SensorNotInititalizedException {
		return pitch;
	}

	@Override
	public double getUnfilteredRoll() throws SensorNotInititalizedException {
		return roll;
	}

	@Override
	protected void _onRead() throws Exception {
        int b1 = device.readU8(3);
		int b2 = device.readU8(2) * 256;
		h255 = (b1 & 0xFF) + b2;
		heading = ((double)h255/10.0);
	
		roll = device.readS8(5);
		pitch = device.readS8(4);
	}
}
