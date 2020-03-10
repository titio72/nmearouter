package com.aboni.nmea.router.services;

import java.time.Instant;

public class DateRangeParameter {

    private final Instant cFrom;
    private final Instant cTo;

    public DateRangeParameter(ServiceConfig config) {
        cFrom = config.getParamAsInstant("date", Instant.now().minusSeconds(86400), "yyyyMMddHHmm");
        cTo = config.getParamAsInstant("dateTo", Instant.now(), "yyyyMMddHHmm");
    }

    public Instant getFrom() {
        return cFrom;
    }

    public Instant getTo() {
        return cTo;
    }
}
