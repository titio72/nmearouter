package com.aboni.nmea.router.n2k.can;

public interface CANErrorCallback {
    void onError(byte[] msg, String errorMessage);
}
