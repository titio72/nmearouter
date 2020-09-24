package com.aboni.nmea.router.n2k.messages;

import java.time.Instant;

public interface N2KSystemTime {

    int getSID();

    Instant getTime();

    String getTimeSourceType();
}
