package com.aboni.nmea.router.n2k;

public interface AISStaticData {

    int getMessageId();

    String getMMSI();

    String getRepeatIndicator();

    String getName();

    String getCallSign();

    String getAISClass();

    double getLength();

    double getBeam();

    String getTypeOfShip();

    String getAisTransceiverInfo();

}
