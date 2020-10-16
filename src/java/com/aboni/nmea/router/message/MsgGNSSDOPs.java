package com.aboni.nmea.router.message;

public interface MsgGNSSDOPs extends Message {

    int getSID();

    double getHDOP();

    double getVDOP();

    double getTDOP();

    GNSSFix getFix();

    String getFixDescription();
}
