package com.aboni.sensors;

import java.io.IOException;

import com.aboni.misc.Utils;
import com.aboni.utils.HWSettings;
import com.aboni.utils.ServerLog;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class SensorCompass extends ASensorCompass {

	private final SensorHMC5883 magnetometer;
	private final SensorMPU6050 gyro;
	
	private final MagnetometerToCompass compass;
	
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
			Utils.pause(500);
			// if the gyro hasn't started the magnetometer will fail to initialize 
            magnetometer.init(bus);
		}
	}

    /**
	 * Get the bearing compensated with the tilt data and deviation.
	 * @return
	 */
    @Override
	public double getUnfilteredSensorHeading() {
	    double[] mag_raw = magnetometer.getMagVector();
	    double[] acc_raw = gyro.readRawAccel();
	    return compass.getTiltCompensatedHeading(mag_raw, acc_raw); 
	}
    
	public double[] getRotationDegrees() throws SensorNotInititalizedException {
		double[] r = gyro.readAccel();
		return new double[] {Math.toDegrees(r[0]), 
				Math.toDegrees(r[1]), Math.toDegrees(r[2])};
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
