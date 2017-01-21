package com.aboni.autopilot;

public interface Autopilot {
	
	/**
	 * Set the intended direction.
	 * @param h
	 */
	void setTarget(Heading h);
	
	Heading getTarget();
	
	/**
	 * Receives notification of the current magnetic or true heading from the sensors.
	 * @param h				The current heading at the time 'timestamp'
	 * @param timestamp		The timestamp of the measurement.
	 */
	void setHeading(Heading h, double speed, long timestamp);
	
	/**
	 * Receives notification of the current rudder angle from the sensors.
	 * @param h				
	 * @param timestamp		The timestamp of the measurement.
	 */
	void setRudder(Rudder h, long timestamp);

	/**
	 * Receives notification of the current course over ground from the GPS.
	 * @param h				The current course at the time 'timestamp'
	 * @param timestamp		The timestamp of the measurement.
	 */
	void setCOG(Heading h, double sog, long timestamp);
	
	void setCorrectionListener(HeadingListener listener); 

}
