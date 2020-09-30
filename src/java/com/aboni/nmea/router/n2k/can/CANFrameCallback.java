package com.aboni.nmea.router.n2k.can;

public interface CANFrameCallback {
    void onFrame(byte[] frame);
}
