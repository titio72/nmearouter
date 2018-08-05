package com.aboni.sensors;

import java.io.IOException;

import com.aboni.utils.HWSettings;
import com.aboni.utils.ServerLog;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class SensorCompass extends ASensorCompass {

	private SensorHMC5883 magnetometer;
	private SensorMPU6050 gyro;
	
	private MagnetometerToCompass compass;
	
	public SensorCompass() {
		super();
        gyro = new SensorMPU6050();
        magnetometer = new SensorHMC5883();
        compass = new MagnetometerToCompass();
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
    public double getSensorHeadingNotCompensated() {
    	double[] d = getMagReading();
        return compass.getHeadingDegrees(d[0], d[1], d[2]);
    }	
    
    /**
	 * Get the bearing compensated with the tilt data and deviation.
	 * @return
	 */
    @Override
	public double getUnfilteredSensorHeading() throws SensorNotInititalizedException {
	    double[] mag_raw = magnetometer.getMagVector();
	    double[] acc_raw = gyro.readRawAccel();
	    return compass.getTiltCompensatedHeading(mag_raw, acc_raw); 
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

    @Override
    protected void _onLoadConfiguration() {
    	super._onLoadConfiguration();
    	updateCalibration();
    }

    private void updateCalibration() {
	    try {
	        int x = HWSettings.getPropertyAsInteger("calibration.x", 0);
	        int y = HWSettings.getPropertyAsInteger("calibration.y", 0);
	        int z = HWSettings.getPropertyAsInteger("calibration.z", 0);
	        compass.setCalibration(x, y, z);
        } catch (Exception e) {
            ServerLog.getLogger().Error("Cannot load compass calibration!", e);
        }
    }

    @Override
    protected void _onRead() throws Exception {
        gyro.read();
        magnetometer.read();
    }

	@Override
	public double getUnfilteredPitch() throws SensorNotInititalizedException {
		double[] rot = getRotationDegrees();
		return rot[1];
	}

	@Override
	public double getUnfilteredRoll() throws SensorNotInititalizedException {
		double[] rot = getRotationDegrees();
		return rot[0];
	}
}
