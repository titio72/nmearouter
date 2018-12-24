package com.aboni.nmea.router.agent.impl.track;

import com.aboni.geo.GeoPositionT;
import com.aboni.utils.db.Event;

public class TrackEvent implements Event {

	private final GeoPositionT p;
	private final boolean anchor;
	private final double dist;
	private final double speed;
	private final double maxSpeed;
	private final int interval;
	
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
