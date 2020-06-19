package com.aboni.nmea.router.n2k;

public interface CANBOATStream {
    CANBOATPGNMessage getMessage(String sMessage);
}
