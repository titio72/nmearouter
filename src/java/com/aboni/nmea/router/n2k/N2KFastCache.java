package com.aboni.nmea.router.n2k;

public interface N2KFastCache {
    void setCallback(N2KMessageHandler callback);

    void onMessage(N2KMessage msg);

    void cleanUp();

    void onTimer();
}
