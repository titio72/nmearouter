package com.aboni.geo;

import java.io.InputStream;

public interface DeviationManager {

	/**
	 * Get the magnetic north given the compass reading by applying the deviation map.
	 * @param reading The compass reading in decimal degrees to be converted.
	 * @return The magnetic north in decimal degrees [0..360].
	 */
	double getMagnetic(double reading);

	/**
	 * Get the compass north given the magnetic reading by applying the deviation map.
	 * @param magnetic Magnetic north in decimal degrees to be converted.
	 * @return The compass north in decimal degrees [0..360].
	 */
	@SuppressWarnings("unused")
	double getCompass(double magnetic);

	void reset();

	boolean load(InputStream s);
}