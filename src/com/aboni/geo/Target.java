package com.aboni.geo;

import net.sf.marineapi.nmea.util.Position;

public class Target {
	private Position target;
	private PositionHistory history;
	
	public Target(Position target) {
		this.setTarget(target);
		this.history = new PositionHistory();
	}
	
	public void start() {
		history.reset();
	}
	
	public void setPosition(GeoPositionT currentPosition) {
		history.addPosition(currentPosition);
	}
	
	public GeoPositionT getLastKnownPosition() {
		return history.getLastKnownPosition();
	}
	
	public Course getLastKnownCourse() {
		return history.getLastKnownCourse();
	}
	
	public Course getCourseToTarget() {
		GeoPositionT t = getLastKnownPosition();
		if (t!=null) {
			return new Course(t, getTarget());
		} else {
			return null;
		}
	}
	
	/**
	 * Estimate the remaining time to target assuming the current speed can be maintained to destination.
	 * @return The estimate remaining time in milliseconds
	 */
	public long getEstimateRemainingTime() {
		Course toTarget = getCourseToTarget();
		Course current = getLastKnownCourse();
		if (toTarget != null && current != null) {
			return (long) ((toTarget.getDistance() / current.getSpeed()) * 60 * 60 * 1000);
		} else {
			return 0;
		}
	}

	/**
	 * @return the target
	 */
	public Position getTarget() {
		return target;
	}

	private void setTarget(Position target) {
		this.target = target;
	}
	
	
}	
