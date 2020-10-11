package com.aboni.nmea.router.message;

public interface MsgEnvironmentTempAndPressure extends Message {

    int getSID();

    double getWaterTemp();

    double getAirTemp();

    double getAtmosphericPressure();
}
