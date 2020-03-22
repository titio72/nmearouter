package com.aboni.sensors;

import com.aboni.geo.DeviationManager;
import com.aboni.geo.DeviationManagerImpl;
import com.aboni.misc.DataFilter;
import com.aboni.utils.Constants;
import com.aboni.utils.HWSettings;
import com.aboni.utils.ServerLog;

import java.io.File;
import java.io.FileInputStream;

public abstract class ASensorCompass extends I2CSensor {

	private final DeviationManager devManager;
	
	private double compassSmoothing = 0.75;
	private double attitudeSmoothing = 0.75;
	private long lastModifiedDevTable;
	
	private double pitch;
	private double roll;
	private double head;
	
	public ASensorCompass() {
        devManager = new DeviationManagerImpl();
	}

	/**
     * Get pitch in degrees without any smoothing.
     *
     * @return The pitch value in degrees
     * @throws SensorNotInitializedException When the sensor has not been initialized
     */
    public abstract double getUnfilteredPitch() throws SensorNotInitializedException;

    /**
     * Get the roll in degrees without smoothing.
     *
     * @return The value in degrees
     * @throws SensorNotInitializedException When the sensor has not been initialized
     */
    public abstract double getUnfilteredRoll() throws SensorNotInitializedException;

    /**
     * Get the the heading in degrees [0..360].
     *
     * @return The heading in degrees
     * @throws SensorNotInitializedException When the sensor has not been initialized
     */
    public abstract double getUnfilteredSensorHeading() throws SensorNotInitializedException;

	@SuppressWarnings("unused")
	public double getPitch() {
		return pitch;
	}

	@SuppressWarnings("unused")
	public double getRoll() {
		return roll;
	}

    public double getSensorHeading() {
        return head;
    }

    /**
     * Get the the heading in degrees compensated with the deviation table[0..360].
	 * @return the heading in degrees
	 */
	public double getHeading() {
	    return devManager.getMagnetic(getSensorHeading());
	}
	
    public void loadConfiguration() {
        updateDeviationTable();
        attitudeSmoothing = HWSettings.getPropertyAsDouble("attitude.smoothing", 0.75);
        compassSmoothing = HWSettings.getPropertyAsDouble("compass.smoothing", 0.75);
    	onLoadCompassConfiguration();
    }

    protected void onLoadCompassConfiguration() {
    }
    
    private void updateDeviationTable() {
        try {
        	File f = new File(Constants.DEVIATION);
        	if (f.exists() && f.lastModified()>lastModifiedDevTable) {
        		ServerLog.getLogger().info("Reloading deviation table.");
        		lastModifiedDevTable = f.lastModified();
	        	try (FileInputStream s = new FileInputStream(f)) {
					devManager.reset();
					if (!devManager.load(s)) {
						ServerLog.getLogger().error("CompassSensor cannot load deviation table!");
					}
				}
        	}
		} catch (Exception e) {
			ServerLog.getLogger().error("Cannot load deviation table!", e);
		}
    }

    @Override
    protected void readSensor() throws SensorException {
    	onCompassRead();
		pitch = DataFilter.getLPFReading(attitudeSmoothing, pitch, getUnfilteredPitch());
		roll = DataFilter.getLPFReading(attitudeSmoothing, roll, getUnfilteredRoll());
		head = DataFilter.getLPFReading(compassSmoothing, head, getUnfilteredSensorHeading());
    }
    
    protected abstract void onCompassRead() throws SensorException;
    
    @Override
    public String getSensorName() {
        return "COMPASS";
    }
}
