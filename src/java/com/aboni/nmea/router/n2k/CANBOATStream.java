package com.aboni.nmea.router.n2k;

public interface CANBOATStream {
    PGNMessage getMessage(String sMessage);
}
