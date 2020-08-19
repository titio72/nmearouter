package com.aboni.nmea.router.n2k.messages;

public interface N2KSeatalkPilotWindDatum {

    int PGN = 65345;

    double getRollingAverageWind();

    double getWindDatum();
}
