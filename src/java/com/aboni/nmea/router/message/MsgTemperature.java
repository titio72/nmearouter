package com.aboni.nmea.router.message;

public interface MsgTemperature extends Message {

    int getSID();

    int getInstance();

    String getSource();

    double getTemperature();

    double getSetTemperature();
}
