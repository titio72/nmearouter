package com.aboni.sensors;

import java.io.File;
import java.io.FileInputStream;

import com.aboni.geo.DeviationManagerImpl;
import com.aboni.utils.Constants;
import com.aboni.utils.DataFilter;
import com.aboni.utils.HWSettings;
import com.aboni.utils.ServerLog;

public abstract class ASensorCompass extends I2CSensor {

	private DeviationManagerImpl devManager;
	
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
	 * Get pitch in degrees.
	 * @return
	 */
	public abstract double getUnfilteredPitch() throws SensorNotInititalizedException;
	
	/**
	 * Get the roll in degrees.
	 * @return
	 */
	public abstract double getUnfilteredRoll() throws SensorNotInititalizedException;
	
	/**
	 * Get the the heading in degrees [0..360].
	 * @return
	 * @throws SensorNotInititalizedException
	 */
	public abstract double getUnfilteredSensorHeading() throws SensorNotInititalizedException;
	
	public double getPitch() throws SensorNotInititalizedException {
		return pitch;
	}
	
	public double getRoll() throws SensorNotInititalizedException {
		return roll;
	}
	
	public double getSensorHeading() throws SensorNotInititalizedException {
		return head;
	}
	
	/**
	 * Get the the heading in degrees compensated with the deviation table[0..360].
	 * @return
	 * @throws SensorNotInititalizedException
	 */
	public double getHeading() throws SensorNotInititalizedException {
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
	        	devManager.load(s);
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