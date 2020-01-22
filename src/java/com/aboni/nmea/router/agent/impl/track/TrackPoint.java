package com.aboni.nmea.router.agent.impl.track;

import com.aboni.geo.GeoPositionT;

public class TrackPoint {

    private final GeoPositionT position;
    private final boolean anchor;
    private final double distance;
    private final double averageSpeed;
    private final double maxSpeed;
    private final int period;
    private final Integer tripId;
    private final EngineStatus engine;

    public TrackPoint(GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int period) {
        this(p, anchor, dist, speed, maxSpeed, period, EngineStatus.UNKNOWN, null);
    }

    public TrackPoint(GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int period, EngineStatus engine) {
        this(p, anchor, dist, speed, maxSpeed, period, engine, null);
    }

    public TrackPoint(GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int period, int tripId) {
        this(p, anchor, dist, speed, maxSpeed, period, EngineStatus.UNKNOWN, tripId);
    }

    public TrackPoint(GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int period, EngineStatus engine, Integer tripId) {
        this.position = p;
        this.anchor = anchor;
        this.distance = dist;
        this.averageSpeed = speed;
        this.maxSpeed = maxSpeed;
        this.period = period;
        this.engine = engine;
        this.tripId = tripId;
    }

    public TrackPoint(TrackPoint point, int tripId) {
        this.position = point.position;
        this.anchor = point.anchor;
        this.distance = point.distance;
        this.averageSpeed = point.averageSpeed;
        this.maxSpeed = point.maxSpeed;
        this.period = point.period;
        this.engine = point.engine;
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