package com.aboni.autopilot;

public class BoatData {
	private long lastHeadingTimeStamp;
	private Heading lastHeading;
	private double lastSpeed;

	private long lastRudderTimeStamp;
	private Rudder lastRudder;

	private double lastSOG;
	private Heading lastCOG;
	private long lastCOGTimeStamp;
	
	
	public BoatData() {
	}

	/**
	 * @param lastRudder the lastRudder to set
	 */
	public void setRudder(Rudder lastRudder, long timestamp) {
		this.lastRudder = lastRudder;
		this.lastRudderTimeStamp = timestamp;
	}

	/**
	 * @return the lastRudderTimeStamp
	 */
	public long getLastRudderTimeStamp() {
		return lastRudderTimeStamp;
	}

	/**
	 * @return the lastRudder
	 */
	public Rudder getLastRudder() {
		return lastRudder;
	}

	/**
	 * 
	 */
	public void setCOG(Heading cog, double sog, long timestamp) {
		this.lastCOG = cog;
		this.lastSOG = sog;
		this.lastCOGTimeStamp = timestamp;
	}
	
	public double getLastSOG() {
		return lastSOG;
	}
	
	public Heading getLastCOG() {
		return lastCOG;
	}
	
	public long getLastCOGTimeStamp() {
		return lastCOGTimeStamp;
	}
	
	/**
	 * @param lastHeadingTimeStamp the lastHeadingTimeStamp to set
	 */
	public void setHeading(Heading heading, double speed, long timeStamp) {
		this.lastHeadingTimeStamp = timeStamp;
		this.lastSpeed = speed;
		this.lastHeading = heading;
	}

	/**
	 * @return the lastSpeed
	 */
	public double getLastSpeed() {
		return lastSpeed;
	}

	/**
	 * @return the lastHeading
	 */
	public Heading getLastHeading() {
		return lastHeading;
	}

	/**
	 * @return the lastHeadingTimeStamp
	 */
	public long getLastHeadingTimeStamp() {
		return lastHeadingTimeStamp;
	}
}