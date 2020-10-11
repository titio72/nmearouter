package com.aboni.nmea.router.message;

public interface MsgEnvironment extends Message {

    int getSID();

    String getHumiditySource();

    String getTempSource();

    double getTemperature();

    double getHumidity();

    double getAtmosphericPressure();
}
