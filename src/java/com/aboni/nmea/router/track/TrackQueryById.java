package com.aboni.nmea.router.track;

public class TrackQueryById implements TrackQuery {

    private final int trackId;

    public TrackQueryById(int id) {
        this.trackId = id;
    }

    public int getTrackId() {
        return trackId;
    }
}
