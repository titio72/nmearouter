package com.aboni.nmea.router.n2k.messages;

public interface N2KSeatalkPilotLockedHeading {

    int PGN = 65360;

    double getLockedHeadingMagnetic();

    double getLockedHeadingTrue();
}
