package com.aboni.autopilot;

public class Angle {
	
	private double angle;
	
	public static Angle fromDegree(double degrees) {
		return new Angle(Math.toRadians(degrees)); 
	}
	
	public Angle(double a) {
		setAngle(a);
	}
	
	protected Angle(Angle h) {
		setAngle(h.getAngle());
	}
	
	protected void setAngle(double a) {
		int sign = (int) (a/Math.abs(a));
		double _a = Math.abs(a);
		a = sign * (_a - ((int)(_a/Math.PI/2))*Math.PI*2);
		//a = (a > Math.PI) ? ( a - 2 * Math.PI) : a; 
		this.angle = a;
	}
	
	/**
	 * Heading expressed in radiant.
	 * @return The heading.
	 */
	
	public double getAngle() {
		return angle;
	}
	
	protected double _diff(Angle reference) {
		return getAngle() - reference.getAngle();
	}
	
	public double toDegree() {
		return Math.toDegrees(getAngle());
	}
	
	public static void main(String[] args) {
		
		System.out.println(Angle.fromDegree(270).toDegree());
		System.out.println(Angle.fromDegree(-90).toDegree());
		System.out.println(Angle.fromDegree(-450).toDegree());
		System.out.println(Angle.fromDegree(90).toDegree());
		System.out.println(Angle.fromDegree(450).toDegree());
		System.out.println(Angle.fromDegree(180).toDegree());
		System.out.println(Angle.fromDegree(-180).toDegree());
		
		
	}
}
