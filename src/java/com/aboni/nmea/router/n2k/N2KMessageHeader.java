package com.aboni.nmea.router.n2k;

import java.time.Instant;

public interface N2KMessageHeader {

    int getPgn();

    int getSource();

    int getDest();

    int getPriority();

    Instant getTimestamp();
}
