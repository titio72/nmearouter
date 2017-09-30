package com.aboni.nmea.router.agent.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.utils.db.Event;

public class TrackEvent implements Event {

	private GeoPositionT p;
	private boolean anchor;
	private double dist;
	private double speed;
	private double maxSpeed;
	private int interval;
	
	public TrackEvent (GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int interval) {
		this.p = p;
		this.anchor = anchor;
		this.dist = dist;
		this.speed = speed;
		this.maxSpeed = maxSpeed;
		this.interval = interval;
	}
	
	@Override
	public long getTime() {
		return p.getTimestamp();
	}

	public GeoPositionT getP() {
		return p;
	}

	public boolean isAnchor() {
		return anchor;
	}

	public double getDist() {
		return dist;
	}

	public double getSpeed() {
		return speed;
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public int getInterval() {
		return interval;
	}

}
