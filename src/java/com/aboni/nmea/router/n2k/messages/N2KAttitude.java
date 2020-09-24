package com.aboni.nmea.router.n2k.messages;

public interface N2KAttitude {

    int getSID();

    double getYaw();

    double getPitch();

    double getRoll();
}
