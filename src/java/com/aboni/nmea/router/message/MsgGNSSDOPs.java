package com.aboni.nmea.router.message;

public interface MsgGNSSDOPs extends Message {

    int getSid();

    double getHDOP();

    double getVDOP();

    double getTDOP();

    GNSSFix getFix();

    String getFixDescription();
}
