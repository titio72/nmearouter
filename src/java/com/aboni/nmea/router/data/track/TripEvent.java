package com.aboni.nmea.router.data.track;

import com.aboni.utils.db.Event;

import javax.validation.constraints.NotNull;

public class TripEvent implements Event {

    private final Trip t;

    public TripEvent(@NotNull Trip t) {
        this.t = t;
    }

    public Trip getTrip() {
        return t;
    }

    @Override
    public long getTime() {
        return t.getEndTS().toEpochMilli();
    }
}
