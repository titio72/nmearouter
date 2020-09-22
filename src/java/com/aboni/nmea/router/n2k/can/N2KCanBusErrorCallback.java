package com.aboni.nmea.router.n2k.can;

public interface N2KCanBusErrorCallback {
    void onError(byte[] msg);
}
