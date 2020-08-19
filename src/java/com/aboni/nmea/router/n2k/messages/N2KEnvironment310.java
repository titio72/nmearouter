package com.aboni.nmea.router.n2k.messages;

public interface N2KEnvironment310 {

    int PGN = 130310;

    int getSID();

    double getWaterTemp();

    double getAirTemp();

    double getAtmosphericPressure();
}
