package com.aboni.sensors;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import java.io.IOException;

public interface Sensor {

	void init() throws IOException, UnsupportedBusNumberException;

	String getSensorName();

	void read() throws SensorNotInititalizedException;

}