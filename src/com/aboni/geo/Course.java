package com.aboni.geo;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import net.sf.marineapi.nmea.util.Position;

public class Course {

	private Position p0;
	private Position p1;
	private double distance;
	private double speed;
	private double cog;
	
	public Course(Position p0, Position p1) {
		this.p0 = p0;
		this.p1 = p1;
		calc();
		calcSpeed();
	}

	private void calc() {
		GeodesicData d = Geodesic.WGS84.Inverse(p0.getLatitude(), p0.getLongitude(), p1.getLatitude(), p1.getLongitude());
		distance = d.s12 / 1852.0;
		cog = Utils.normalizeDegrees0_360((d.azi2 + d.azi1) / 2.0);
		calcSpeed();
	}
	
	public long getInterval() {
		if (p0 instanceof GeoPositionT && p1 instanceof GeoPositionT)
			return ((GeoPositionT)p1).getTimestamp() - ((GeoPositionT)p0).getTimestamp();
		else 
			return 0;
	}
	
	private void calcSpeed() {
		long dTime = getInterval();
		if (dTime>0) {
			double dT = ((double)dTime) / 60.0 / 60.0 / 1000.0; //hours
			speed = distance / dT;
		} else {
			speed = Double.NaN;
		}
	}
	
	/**
	 * The distance between in nautical miles.
	 * @return the distance.
	 */
	public double getDistance() {
		return distance;
	}
	
	/**
	 * The "Course over ground".
	 * @return The CoG.
	 */
	public double getCOG() {
		return cog;
	}
	
	/**
	 * The speed in Knots.
	 * @return the speed value.
	 */
	public double getSpeed() {
		return speed;
	}
}
