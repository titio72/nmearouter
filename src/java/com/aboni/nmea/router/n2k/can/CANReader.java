package com.aboni.nmea.router.n2k.can;

import com.aboni.nmea.router.n2k.N2KMessageCallback;

public interface CANReader {
    void setFrameCallback(CANFrameCallback callback);

    void setCallback(N2KMessageCallback callback);

    void setErrCallback(CANErrorCallback errCallback);

    boolean onRead(int[] b, int lastByteOffset);

    CANReaderStats getStats();
}
