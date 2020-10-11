package com.aboni.nmea.router.message;

public interface MsgAttitude extends Message {

    int getSID();

    double getYaw();

    double getPitch();

    double getRoll();
}
