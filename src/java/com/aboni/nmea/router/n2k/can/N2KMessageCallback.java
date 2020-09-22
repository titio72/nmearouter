package com.aboni.nmea.router.n2k.can;

import com.aboni.nmea.router.n2k.N2KMessage;

public interface N2KMessageCallback {
    void onRead(N2KMessage msg);
}
