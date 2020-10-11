package com.aboni.nmea.router.message;

public interface MsgRudder extends Message {

    int getInstance();

    double getAngle();

    double getAngleOrder();

    int getDirectionOrder();
}
