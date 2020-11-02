package com.aboni.nmea.router;

import java.time.Instant;
import java.time.ZoneOffset;

public interface TimestampProvider {

    long getNow();

    default Instant getInstant() {
        return Instant.ofEpochMilli(getNow());
    }

    default int getYear() {
        return getInstant().atOffset(ZoneOffset.UTC).getYear();
    }
}
