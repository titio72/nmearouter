package com.aboni.nmea.router.agent.impl.track;

public interface TrackWriter {
    void write(TrackPoint point);
    boolean init();
    void dispose();
}
