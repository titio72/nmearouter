package com.aboni.nmea.router.agent.impl.track;

import com.aboni.geo.GeoPositionT;

public class TrackPoint {

	final GeoPositionT position;
	final boolean anchor;
	final double distance;
	final double averageSpeed;
	final double maxSpeed;
	final int period;


    public TrackPoint(GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int period) {
		this.position = p;
		this.anchor = anchor;
		this.distance = dist;
		this.averageSpeed = speed;
		this.maxSpeed = maxSpeed;
		this.period = period;
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


}