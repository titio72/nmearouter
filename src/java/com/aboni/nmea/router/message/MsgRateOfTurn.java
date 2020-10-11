package com.aboni.nmea.router.message;

public interface MsgRateOfTurn extends Message {

    int getSID();

    double getRateOfTurn();
}
