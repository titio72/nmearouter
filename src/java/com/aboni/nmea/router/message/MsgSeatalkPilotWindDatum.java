package com.aboni.nmea.router.message;

public interface MsgSeatalkPilotWindDatum extends Message {

    double getRollingAverageWind();

    double getWindDatum();
}
