package com.aboni.autopilot;

public class AutoPilotQoS {
	
	private static int MIN = 0;
	private static int MAX = 5;
	private static int DEFAULT = 3;
	
	/**
	 * 0..5
	 */
	private int sensitivity = DEFAULT;

	/**
	 * 0..5
	 */
	private int reactiveness = DEFAULT;
	
	/**
	 * Limit rudder angle
	 */
	private Rudder maxRudder = new Rudder(Math.PI / 3); // 30 degrees ;
	private Rudder minRudder = new Rudder(- Math.PI / 3); // 30 degrees ;
	
	/**
	 * Sensitivity determine how quickly the the pilot will react to heading deviation from intended direction.
	 * The range is between 0 and 5 where 5 is the quickest.
	 * Default is 3;
	 * @return the sensitivity
	 */
	public int getSensitivity() {
		return sensitivity;
	}

	/**
	 * @param sensitivity the sensitivity to set
	 */
	public void setSensitivity(int sensitivity) {
		if (sensitivity < MIN || sensitivity > MAX) throw new RuntimeException("Sensititivity " + sensitivity + " is not valid.");
		else this.sensitivity = sensitivity;
	}

	/**
	 * Reactiveness determines the intensity of corrections.
	 * The valid range is 0..5 - default is 3.
	 * @return the reactiveness
	 */
	public int getReactiveness() {
		return reactiveness;
	}

	/**
	 * @param reactiveness the reactiveness to set
	 */
	public void setReactiveness(int reactiveness) {
		if (reactiveness < MIN || reactiveness > MAX) throw new RuntimeException("Reactiveness " + reactiveness + " is not valid.");
		else this.reactiveness = reactiveness;
	}

	/**
	 * @return the maxRudder
	 */
	public Rudder getMaxRudder() {
		return maxRudder;
	}

	/**
	 * @param maxRudder the maxRudder to set
	 */
	public void setMaxRudder(Rudder maxRudder) {
		this.maxRudder = maxRudder;
	}

	/**
	 * @return the minRudder
	 */
	public Rudder getMinRudder() {
		return minRudder;
	}

	/**
	 * @param minRudder the minRudder to set
	 */
	public void setMinRudder(Rudder minRudder) {
		this.minRudder = minRudder;
	}
}

