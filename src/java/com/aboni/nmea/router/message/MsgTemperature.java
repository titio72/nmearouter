package com.aboni.nmea.router.message;

public interface MsgTemperature extends Message {

    int getSID();

    int getInstance();

    TemperatureSource getTemperatureSource();

    double getTemperature();

    double getSetTemperature();
}
