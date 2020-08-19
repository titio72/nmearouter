package com.aboni.nmea.router.n2k.messages;

public interface N2KWindData {

    int PGN = 130306;

    int getSID();

    double getSpeed();

    double getAngle();

    boolean isApparent();
}
