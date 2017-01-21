package com.aboni.geo;

import net.sf.marineapi.nmea.util.Position;

public class GeoPositionT extends Position {

	public GeoPositionT(long timestamp, Position p) {
		super(p.getLatitude(), p.getLongitude());
		this.timestamp = timestamp;
	}

	public GeoPositionT(long timestamp, double lat, double lon) {
		super(lat, lon);
		this.timestamp = timestamp;
	}

	public GeoPositionT(Position p) {
		this(System.currentTimeMillis(), p);
	}
	
	private long timestamp;

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
}
