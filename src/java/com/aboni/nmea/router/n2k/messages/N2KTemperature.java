package com.aboni.nmea.router.n2k.messages;

public interface N2KTemperature {

    int PGN = 130312;

    int getSID();

    int getInstance();

    String getSource();

    double getTemperature();

    double getSetTemperature();
}
