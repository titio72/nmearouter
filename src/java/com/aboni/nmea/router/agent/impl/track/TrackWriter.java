package com.aboni.nmea.router.agent.impl.track;

import com.aboni.nmea.router.track.TrackPoint;

public interface TrackWriter {
    void write(TrackPoint point);
    boolean init();
    void dispose();
}
