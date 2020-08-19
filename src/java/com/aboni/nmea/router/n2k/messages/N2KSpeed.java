package com.aboni.nmea.router.n2k.messages;

public interface N2KSpeed {

    int PGN = 128259;

    int getSID();

    double getSpeedWaterRef();

    double getSpeedGroundRef();

    String getWaterRefType();

    int getSpeedDirection();
}
