package com.aboni.autopilot;

public class Rudder extends Angle {

	public static Rudder fromDegree(double d) {
		return new Rudder(Math.toRadians(d));
	}
	
	public Rudder(double r) {
		super(r);
	}
	
	public Rudder(Rudder r) {
		super(r);
	}
	
	public static Rudder CENTER = new Rudder(0); 

}
