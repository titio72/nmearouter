package com.aboni.nmea.router.n2k.messages;

public interface N2KHeading {

    int PGN = 127250;

    int getSID();

    double getHeading();

    double getDeviation();

    double getVariation();

    String getReference();

    boolean isTrueHeading();
}
