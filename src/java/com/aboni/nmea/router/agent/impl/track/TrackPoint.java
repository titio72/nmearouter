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

}