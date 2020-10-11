package com.aboni.nmea.router.message;

public interface MsgWindData extends Message {

    int getSID();

    double getSpeed();

    double getAngle();

    boolean isApparent();
}
