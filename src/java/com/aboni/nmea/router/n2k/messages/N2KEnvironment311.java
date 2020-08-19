package com.aboni.nmea.router.n2k.messages;

public interface N2KEnvironment311 {

    int PGN = 130311;

    int getSID();

    String getHumiditySource();

    String getTempSource();

    double getTemperature();

    double getHumidity();

    double getAtmosphericPressure();
}
