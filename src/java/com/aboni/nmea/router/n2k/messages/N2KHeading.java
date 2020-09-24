package com.aboni.nmea.router.n2k.messages;

public interface N2KHeading {

    int getSID();

    double getHeading();

    double getDeviation();

    double getVariation();

    String getReference();

    boolean isTrueHeading();
}
