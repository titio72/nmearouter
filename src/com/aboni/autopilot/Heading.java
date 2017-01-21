package com.aboni.autopilot;

public class Heading extends Angle {
	
	public static Heading fromDegree(double d) {
		return new Heading(Math.toRadians(d));
	}
	
	public Heading(double h) {
		super(h);
	}
	
	public Heading(Heading h) {
		super(h);
	}
	
	/**
	 * Heading expressed in radiant.
	 * @return The heading.
	 */
	public double getHeading() {
		return getAngle();
	}
	
	public Heading diff(Heading reference) {
		return new Heading(_diff(reference));
	}
}
