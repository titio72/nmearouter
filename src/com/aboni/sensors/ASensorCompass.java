package com.aboni.sensors;

import com.aboni.geo.DeviationManager;
import com.aboni.geo.DeviationManagerImpl;
import com.aboni.utils.Constants;
import com.aboni.utils.DataFilter;
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
	 * Get pitch in degrees without any wmoothing.
	 * @return The pitch value in degrees
	 * @throws SensorNotInititalizedException When the sensot has not been initialized
	 */
	public abstract double getUnfilteredPitch() throws SensorNotInititalizedException;
	
	/**
	 * Get the roll in degrees without smoothing.
	 * @return The valkue in degrees
	 * @throws SensorNotInititalizedException When the sensot has not been initialized
	 */
	public abstract double getUnfilteredRoll() throws SensorNotInititalizedException;
	
	/**
	 * Get the the heading in degrees [0..360].
	 * @return The heading in degrees
	 * @throws SensorNotInititalizedException When the sensot has not been initialized
	 */
	public abstract double getUnfilteredSensorHeading() throws SensorNotInititalizedException;

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
	 * @return the heading in dgrees
	 */
	public double getHeading() {
	    return devManager.getMagnetic(getSensorHeading());
	}
	
    public void loadConfiguration() {
        updateDeviationTable();
        attitudeSmoothing = HWSettings.getPropertyAsDouble("attitude.smoothing", 0.75);
        compassSmoothing = HWSettings.getPropertyAsDouble("compass.smoothing", 0.75);
    	_onLoadConfiguration();
    }

    protected void _onLoadConfiguration() {
    }
    
    private void updateDeviationTable() {
        try {
        	File f = new File(Constants.DEVIATION);
        	if (f.exists() && f.lastModified()>lastModifiedDevTable) {
        		ServerLog.getLogger().Info("Reloading deviation table.");
        		lastModifiedDevTable = f.lastModified();
	        	FileInputStream s = new FileInputStream(f); 
				devManager.reset();
	        	if (!devManager.load(s)) {
					ServerLog.getLogger().Error("CompassSensor cannot load deviation table!");
				}
				s.close();
        	}
		} catch (Exception e) {
			ServerLog.getLogger().Error("Cannot load deviation table!", e);
		}
    }

    @Override
    protected void _read() throws Exception {
    	_onRead();
    	pitch = DataFilter.getLPFReading(attitudeSmoothing, pitch, getUnfilteredPitch());
    	roll = DataFilter.getLPFReading(attitudeSmoothing, roll, getUnfilteredRoll());
    	head = DataFilter.getLPFReading(compassSmoothing, head, getUnfilteredSensorHeading());
    }
    
    protected abstract void _onRead() throws Exception;
    
    @Override
    public String getSensorName() {
        return "COMPASS";
    }
}
