package com.aboni.nmea.router.n2k.messages;

import com.aboni.nmea.router.n2k.GNSSFix;

public interface N2KGNSSDOPs {

    int getSid();

    double getHDOP();

    double getVDOP();

    double getTDOP();

    GNSSFix getFix();

    String getFixDescription();
}
