package com.aboni.nmea.router.track;

import com.aboni.sensors.EngineStatus;

public class TrackSample {
    public void setTs(long ts) {
        this.ts = ts;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setEng(EngineStatus eng) {
        this.eng = eng;
    }

    public void setAnchor(boolean anchor) {
        this.anchor = anchor;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public long getTs() {
        return ts;
    }

    public double getDistance() {
        return distance;
    }

    public double getSpeed() {
        return speed;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public EngineStatus getEng() {
        return eng;
    }

    public boolean isAnchor() {
        return anchor;
    }

    public int getPeriod() {
        return period;
    }

    long ts;
    double distance;
    double speed;
    double maxSpeed;
    EngineStatus eng;
    boolean anchor;
    int period;
}
