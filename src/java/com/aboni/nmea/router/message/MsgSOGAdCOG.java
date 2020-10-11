package com.aboni.nmea.router.message;

public interface MsgSOGAdCOG extends Message {

    int getSID();

    double getSOG();

    double getCOG();

    String getCOGReference();

    boolean isTrueCOG();
}
