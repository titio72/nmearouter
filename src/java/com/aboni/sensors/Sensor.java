package com.aboni.sensors;

public interface Sensor {

    void init() throws SensorException;

    String getSensorName();

    void read() throws SensorException;

    long getLastReadingTimestamp();

}