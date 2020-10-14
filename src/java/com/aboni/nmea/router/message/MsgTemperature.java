package com.aboni.nmea.router.message;

public interface MsgTemperature extends MsgGenericTemperature {

    int getSID();

    int getInstance();

    double getSetTemperature();
}
