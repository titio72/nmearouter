package com.aboni.nmea.router.n2k.messages;

public interface N2KRateOfTurn {

    int PGN = 127251;

    int getSID();

    double getRate();
}
