package com.aboni.nmea.router.n2k.can;

import com.aboni.nmea.router.n2k.N2KMessageHandler;

public interface SerialCANReader {

    void setFrameCallback(CANFrameCallback callback);

    void setCallback(N2KMessageHandler callback);

    void setErrCallback(CANErrorCallback errCallback);

    boolean onRead(int[] b, int lastByteOffset);

    CANReaderStats getStats();
}
