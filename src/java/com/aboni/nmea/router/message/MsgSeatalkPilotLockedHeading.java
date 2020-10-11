package com.aboni.nmea.router.message;

public interface MsgSeatalkPilotLockedHeading extends Message {

    double getLockedHeadingMagnetic();

    double getLockedHeadingTrue();
}
