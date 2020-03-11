package com.aboni.nmea.router.track;

public interface TrackWriter {
    void write(TrackPoint point);
    boolean init();
    void dispose();
}
