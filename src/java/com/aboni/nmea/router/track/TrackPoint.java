package com.aboni.nmea.router.track;

import com.aboni.geo.GeoPositionT;
import com.aboni.sensors.EngineStatus;

public interface TrackPoint {

    GeoPositionT getPosition();

    boolean isAnchor();

    double getDistance();

    double getAverageSpeed();

    double getMaxSpeed();

    int getPeriod();

    Integer getTrip();

    EngineStatus getEngine();
}