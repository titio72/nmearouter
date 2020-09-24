package com.aboni.nmea.router.n2k.messages;

public interface N2KRudder {

    int getInstance();

    double getPosition();

    double getAngleOrder();

    int getDirectionOrder();
}
