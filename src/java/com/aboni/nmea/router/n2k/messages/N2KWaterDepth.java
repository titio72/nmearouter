package com.aboni.nmea.router.n2k.messages;

public interface N2KWaterDepth {

    int getSID();

    double getDepth();

    double getOffset();

    double getRange();
}
