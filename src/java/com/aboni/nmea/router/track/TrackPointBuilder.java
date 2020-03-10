package com.aboni.nmea.router.track;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.track.impl.TrackPointBuilderImpl;
import com.aboni.sensors.EngineStatus;

public interface TrackPointBuilder {

    TrackPointBuilderImpl withPoint(TrackPoint point);

    TrackPointBuilderImpl withPosition(GeoPositionT pos);

    TrackPointBuilderImpl withSpeed(double speed, double maxSpeed);

    TrackPointBuilderImpl withAnchor(boolean anchor);

    TrackPointBuilderImpl withTrip(int tripId);

    TrackPointBuilderImpl withoutTrip();

    TrackPointBuilderImpl withDistance(double distance);

    TrackPointBuilderImpl withPeriod(int period);

    TrackPointBuilderImpl withEngine(EngineStatus engine);

    TrackPoint getPoint();
}
