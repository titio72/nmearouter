package com.aboni.nmea.router.message;

public interface MsgSeatalkPilotHeading extends Message {

    double getHeadingMagnetic();

    double getHeadingTrue();
}
