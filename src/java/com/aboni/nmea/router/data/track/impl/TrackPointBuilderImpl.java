package com.aboni.nmea.router.data.track.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.data.track.TrackPoint;
import com.aboni.nmea.router.data.track.TrackPointBuilder;
import com.aboni.sensors.EngineStatus;

public class TrackPointBuilderImpl implements TrackPoint, TrackPointBuilder {

    GeoPositionT position;
    boolean anchor = false;
    double distance;
    double averageSpeed;
    double maxSpeed;
    int period = 30;
    EngineStatus engine = EngineStatus.UNKNOWN;

    @Override
    public synchronized TrackPointBuilderImpl withPoint(TrackPoint point) {
        this.position = point.getPosition();
        this.anchor = point.isAnchor();
        this.distance = point.getDistance();
        this.averageSpeed = point.getAverageSpeed();
        this.maxSpeed = point.getMaxSpeed();
        this.period = point.getPeriod();
        this.engine = point.getEngine();
        return this;
    }

    @Override
    public synchronized TrackPointBuilderImpl withPosition(GeoPositionT pos) {
        position = pos;
        return this;
    }

    @Override
    public synchronized TrackPointBuilderImpl withSpeed(double speed, double maxSpeed) {
        this.maxSpeed = maxSpeed;
        this.averageSpeed = speed;
        return this;
    }

    @Override
    public synchronized TrackPointBuilderImpl withAnchor(boolean anchor) {
        this.anchor = anchor;
        return this;
    }

    @Override
    public synchronized TrackPointBuilderImpl withDistance(double distance) {
        this.distance = distance;
        return this;
    }

    @Override
    public synchronized TrackPointBuilderImpl withPeriod(int period) {
        this.period = period;
        return this;
    }

    @Override
    public synchronized TrackPointBuilderImpl withEngine(EngineStatus engine) {
        this.engine = engine;
        return this;
    }

    @Override
    public synchronized TrackPoint getPoint() {
        return new TrackPointImpl(this);
    }


    @Override
    public GeoPositionT getPosition() {
        return position;
    }

    @Override
    public boolean isAnchor() {
        return anchor;
    }

    @Override
    public double getDistance() {
        return distance;
    }

    @Override
    public double getAverageSpeed() {
        return averageSpeed;
    }

    @Override
    public double getMaxSpeed() {
        return maxSpeed;
    }

    @Override
    public int getPeriod() {
        return period;
    }

    @Override
    public EngineStatus getEngine() {
        return engine;
    }
}