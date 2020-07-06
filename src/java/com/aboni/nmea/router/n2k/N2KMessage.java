package com.aboni.nmea.router.n2k;

public interface N2KMessage {

    N2KMessageHeader getHeader();

    byte[] getData();
}
