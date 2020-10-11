package com.aboni.nmea.router.message;

import java.time.Instant;

public interface MsgSystemTime extends Message {

    int getSID();

    Instant getTime();

    String getTimeSourceType();
}
