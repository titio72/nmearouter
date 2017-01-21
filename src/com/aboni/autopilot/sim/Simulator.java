package com.aboni.autopilot.sim;

import java.util.Random;

import com.aboni.autopilot.AutoPilotImpl;
import com.aboni.autopilot.AutoPilotQoS;
import com.aboni.autopilot.Autopilot;
import com.aboni.autopilot.Heading;
import com.aboni.autopilot.Rudder;

public class Simulator {
	
	private long timeStart;
	
	private Random r = new Random();
	
	private double headingShiftTendency = 0.0; // -PI,+PI radiant per second
	
	private long periodOfCiclicPerburbance = 30000; // mseconds

	private double amplitudeOfCiclicPerburbance = Math.toRadians(15); // -PI,+PI radiant per second

	private Heading seaDirection = new Heading(0.0);
	
	private double amplitudeOfRandomPerturbance = Math.toRadians(10); // -Pi, +PI radiant per second

	private double boatResponsivness = 1 / 3; // Radiant per second per rudder radiant.  
	
	/**
	 * @return the seaDirection
	 */
	public Heading getSeaDirection() {
		return seaDirection;
	}

	/**
	 * @param seaDirection the seaDirection to set
	 */
	public void setSeaDirection(Heading seaDirection) {
		this.seaDirection = seaDirection;
	}

	public double getHeadingShiftTendency() {
		return headingShiftTendency;
	}

	public void setHeadingShiftTendency(double headingShiftTendency) {
		this.headingShiftTendency = headingShiftTendency;
	}

	public long getPeriodOfCiclicPerburbance() {
		return periodOfCiclicPerburbance;
	}

	public void setPeriodOfCiclicPerburbance(long periodOfCiclicPerburbance) {
		this.periodOfCiclicPerburbance = periodOfCiclicPerburbance;
	}

	public double getAmplitudeOfRandomPerturbance() {
		return amplitudeOfRandomPerturbance;
	}

	public void setAmplitudeOfRandomPerturbance(double amplitudeOfRandomPerturbance) {
		this.amplitudeOfRandomPerturbance = amplitudeOfRandomPerturbance;
	}

	public double getAmplitudeOfCiclicPerburbance() {
		return amplitudeOfCiclicPerburbance;
	}

	public void setAmplitudeOfCiclicPerburbance(double amplitudeOfCiclicPerburbance) {
		this.amplitudeOfCiclicPerburbance = amplitudeOfCiclicPerburbance;
	}  
	
	public void start(long t) {
		timeStart = (t==-1) ? System.currentTimeMillis() : t;
	}

	/**
	 * Get the speed of heading changes per rudder radiant.
	 * I.e. The boat will turn dT * responsiveness * rudder
	 * This is a first order approx valid for small rudder - default is approx 1/3 
	 * (for each 3 degrees of steering the boat turns 3 degrees)  
	 * @return
	 */
	public double getBoatResponsivness() {
		return boatResponsivness;
	}

	public void setBoatResponsivness(double boatResponsivness) {
		this.boatResponsivness = boatResponsivness;
	}

	/**
	 * Get a random perturbance expressed in rad/sec
	 * @return
	 */
	public double getPerturbance(long time, Heading boatH, long dtime) {
		long t = (time==-1) ? (System.currentTimeMillis() - timeStart) : (time - timeStart);
		double periods = t % getPeriodOfCiclicPerburbance();
		
		double p = (headingShiftTendency
				+ ( r.nextDouble() * getAmplitudeOfRandomPerturbance() ) 
				+ Math.cos(getSeaDirection().diff(boatH).getAngle()) * ( Math.sin( 2 * Math.PI * periods / getPeriodOfCiclicPerburbance() ) ) * getAmplitudeOfCiclicPerburbance()
				)  * ((double)dtime/1000.0);  
				
		return p;
	}
	
	/**
	 * 
	 * @param speed		The boat speed in Knots
	 * @param r			The current rudder angle
	 * @param deltaT	The time the rudder is kept in the same position at the same speed
	 * @return			The expected turn in radiant
	 */
	public double getTurn(double speed, Rudder r, long dTime) {
		double speedMS = speed * 1852.0 / 3600.0; 
		double K = 1.5;
		double res = r.getAngle() * speedMS * K * ((double)dTime/1000.0);
		return res;
	}
	
	public static void main(String[] args) {
		AutoPilotQoS qos = new AutoPilotQoS();
		AutoPilotImpl ap = new AutoPilotImpl(qos);
		ap.setTarget(new Heading(0));
		
		Simulator s = new Simulator();
		s.setAmplitudeOfRandomPerturbance(0);
		s.setAmplitudeOfCiclicPerburbance(Math.toRadians(5.0));
		s.setSeaDirection(new Heading(0.0));
		s.start(0);
		
		
		int sampling = 500;
		double newHeading = 0.0;
		ap.setRudder(new Rudder(0.0), 0);
		ap.setHeading(new Heading(0.0), 5, 0);
		for (int i = 0; i<1000 * 60 * 5; i+=sampling) {
			
			Rudder r = ap.getLastRudder();
			Heading h = ap.getLastHeading();
			
			double x = s.getPerturbance(i, h, sampling);
			double y = s.getTurn(5, r, sampling);
			newHeading += (x + y);

			ap.setHeading(new Heading(newHeading), 5, i);

			// calc and apply correction
			Rudder correction = ap.calcCorrection();
			ap.setRudder(new Rudder(correction), i);
			
			System.out.format("%02d:%02d.%03d - Pert. %.3f - Turn %.3f - Corr. %.3f - New Heading %.3f%n", 
						(i/1000)/60 , (i/1000)%60, i%1000, 
						Math.toDegrees(x), 
						Math.toDegrees(y), 
						correction.toDegree(), 
						Math.toDegrees(newHeading)); // + " " + x + " " + (d/i)*10 + " " + y);
		}
	}
}
