package com.aboni.nmea.router.n2k.messages;

public interface N2KSeatalkPilotHeading {

    int PGN = 65359;

    double getHeadingMagnetic();

    double getHeadingTrue();
}
