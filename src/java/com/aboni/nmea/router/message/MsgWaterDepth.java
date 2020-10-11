package com.aboni.nmea.router.message;

public interface MsgWaterDepth extends Message {

    int getSID();

    double getDepth();

    double getOffset();

    double getRange();
}
