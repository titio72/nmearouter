package com.aboni.nmea.router.agent.impl.track;

import com.aboni.geo.GeoPositionT;

public class TrackPoint {
	GeoPositionT position;
	boolean anchor;
	double distance;
	double averageSpeed;
	double maxSpeed;
	int period;
	
	public TrackPoint() {
	}

	public TrackPoint(GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int period) {
		this.position = p;
		this.anchor = anchor;
		this.distance = dist;
		this.averageSpeed = speed;
		this.maxSpeed = maxSpeed;
		this.period = period;
	}

}