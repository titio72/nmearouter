package com.aboni.nmea.router.agent.impl.track;

import com.aboni.geo.GeoPositionT;
import com.aboni.sensors.EngineStatus;

public class TrackPoint {

    private final GeoPositionT position;
    private final boolean anchor;
    private final double distance;
    private final double averageSpeed;
    private final double maxSpeed;
    private final int period;
    private final Integer tripId;
    private final EngineStatus engine;

    public static TrackPoint newInstanceWithTrip(GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int period, int tripId) {
        return new TrackPoint(p, anchor, dist, speed, maxSpeed, period, EngineStatus.UNKNOWN, tripId);
    }

    public static TrackPoint newInstanceBase(GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int period) {
        return new TrackPoint(p, anchor, dist, speed, maxSpeed, period, EngineStatus.UNKNOWN, null);
    }

    public static TrackPoint newInstanceWithEngine(GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int period, EngineStatus engine) {
        return new TrackPoint(p, anchor, dist, speed, maxSpeed, period, engine, null);
    }

    public static TrackPoint newInstance(GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int period, EngineStatus engine, Integer tripId) {
        return new TrackPoint(p, anchor, dist, speed, maxSpeed, period, engine, tripId);
    }

    public static TrackPoint clone(TrackPoint point) {
        return new TrackPoint(point.position, point.anchor, point.distance, point.averageSpeed, point.maxSpeed, point.period, point.engine, point.tripId);
    }

    public static TrackPoint cloneOverride(TrackPoint point, EngineStatus engine, int tripId) {
        return new TrackPoint(point.position, point.anchor, point.distance, point.averageSpeed, point.maxSpeed, point.period, engine, tripId);
    }

    public static TrackPoint cloneOverrideEngine(TrackPoint point, EngineStatus engine) {
        return new TrackPoint(point.position, point.anchor, point.distance, point.averageSpeed, point.maxSpeed, point.period, engine, point.tripId);
    }

    public static TrackPoint cloneOverrideTrip(TrackPoint point, int tripId) {
        return new TrackPoint(point.position, point.anchor, point.distance, point.averageSpeed, point.maxSpeed, point.period, point.engine, tripId);
    }

    private TrackPoint(GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int period, EngineStatus engine, Integer tripId) {
        this.position = p;
        this.anchor = anchor;
        this.distance = dist;
        this.averageSpeed = speed;
        this.maxSpeed = maxSpeed;
        this.period = period;
        this.engine = engine;
        this.tripId = tripId;
    }

    public GeoPositionT getPosition() {
        return position;
    }

    public boolean isAnchor() {
        return anchor;
    }

    public double getDistance() {
        return distance;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public int getPeriod() {
        return period;
    }

    public Integer getTrip() {
        return tripId;
    }

    public EngineStatus getEngine() {
        return engine;
    }
}