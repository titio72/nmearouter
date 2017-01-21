package com.aboni.sensors;

import java.io.IOException;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public interface Sensor {

	void init() throws IOException, UnsupportedBusNumberException;

	String getSensorName();

	long getReadAge();

	void read() throws SensorNotInititalizedException;

}