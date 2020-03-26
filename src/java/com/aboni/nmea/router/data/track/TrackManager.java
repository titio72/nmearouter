package com.aboni.nmea.router.data.track;

import com.aboni.geo.GeoPositionT;
import net.sf.marineapi.nmea.util.Position;

public interface TrackManager {
    long getPeriod();

    boolean isStationary();

    TrackPoint processPosition(GeoPositionT p, double sog);

    void setPeriod(long period);

    long getStaticPeriod();

    void setStaticPeriod(long period);

    GeoPositionT getLastTrackedPosition();

    Position getAverage();
}
