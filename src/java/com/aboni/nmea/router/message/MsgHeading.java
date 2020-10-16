package com.aboni.nmea.router.message;

public interface MsgHeading extends Message {

    int getSID();

    double getHeading();

    double getDeviation();

    double getVariation();

    DirectionReference getReference();

    boolean isTrueHeading();
}
