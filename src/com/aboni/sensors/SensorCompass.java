package com.aboni.sensors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.aboni.geo.DeviationManager;
import com.aboni.utils.ServerLog;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class SensorCompass extends I2CSensor {

	private SensorHMC5883 magnetometer;
	private SensorMPU6050 gyro;
	
	private MagnetometerToCompass compass;
	private DeviationManager devManager;
	
	private long lastModifiedDevTable;
	
	public SensorCompass() {
        gyro = new SensorMPU6050();
        magnetometer = new SensorHMC5883();
        compass = new MagnetometerToCompass();
        devManager = new DeviationManager();
	}

	@Override
	protected void _init(int bus) throws IOException, UnsupportedBusNumberException {
		if (!isInitialized()) {
		    // initialize gyro first
			gyro.init(bus);
			// not clear if it's necessary
			try { Thread.sleep(500); } catch (InterruptedException e) {}
			// if the gyro hasn't started the magnetometer will fail to initialize 
            magnetometer.init(bus);
		}
	}
	
	/**
	 * Get the rotation of the sensor for the 3 axis (used to calculate pitch and roll).
	 * @return The rotation as a vector of double respectively for x, y & z axis 
	 * @throws SensorNotInititalizedException
	 */
	public double[] getRotation() throws SensorNotInititalizedException {
	    return gyro.readAccelDegrees();
	}
    
    /**
     * Get the reading without adjustment (tilt and deviation)
     * @return The reading in degrees.
     */
    public double getSensorHeading() {
    	double[] d = getMagReading();
        return compass.getHeadingDegrees(d[0], d[1], d[2]);
    }	
    
    /**
	 * Get the bearing compensated with the tilt data and deviation.
	 * @return
	 */
	public double getHeading() throws SensorNotInititalizedException {
	    double[] mag_raw = magnetometer.getMagVector();
	    double[] acc_raw = gyro.readRawAccel();
	    double compassAngle = compass.getTiltCompensatedHeading(mag_raw, acc_raw); 
	    return devManager.getMagnetic(compassAngle);
	}
    
	public double[] getRotationDegrees() throws SensorNotInititalizedException {
		double[] r = gyro.readAccel();
		return new double[] {Math.toDegrees(r[0]), 
				Math.toDegrees(r[1]), Math.toDegrees(r[2])};
	}

	/**
	 * Raw magnetic readings on the 3 axis.
	 * @return A vector of doubles for the x,y & z readings. 
	 */
    public double[] getMagReading() {
        return magnetometer.getMagVector();
    }

    public void loadConfiguration() {
    	updateCalibration();
        updateDeviationTable();
    }

    private void updateCalibration() {
        Properties configuration = getProps().getProperties();
    	if (configuration!=null) {
    	    try {
    	        int x = Integer.parseInt(configuration.getProperty("calibration.x"));
    	        int y = Integer.parseInt(configuration.getProperty("calibration.y"));
    	        int z = Integer.parseInt(configuration.getProperty("calibration.z"));
    	        compass.setCalibration(x, y, z);
            } catch (Exception e) {
                ServerLog.getLogger().Error("Cannot load compass calibration!", e);
            }
    	}
    }

    private void updateDeviationTable() {
        try {
        	File f = new File("deviation.csv");
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
    public String getSensorName() {
        return "COMPASS";
    }

    @Override
    protected void _read() throws Exception {
        gyro.read();
        magnetometer.read();
    }
}
