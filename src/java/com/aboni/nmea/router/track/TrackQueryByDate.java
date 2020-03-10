package com.aboni.nmea.router.track;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class TrackQueryByDate implements TrackQuery {

    private final Instant from;
    private final Instant to;

    public TrackQueryByDate(@NotNull Instant from, @NotNull Instant to) {
        this.from = from;
        this.to = to;
    }

    public Instant getFrom() {
        return from;
    }

    public Instant getTo() {
        return to;
    }
}
