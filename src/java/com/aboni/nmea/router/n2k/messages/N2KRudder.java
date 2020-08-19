package com.aboni.nmea.router.n2k.messages;

public interface N2KRudder {

    int PGN = 127245;

    int getInstance();

    double getPosition();

    double getAngleOrder();

    int getDirectionOrder();
}
