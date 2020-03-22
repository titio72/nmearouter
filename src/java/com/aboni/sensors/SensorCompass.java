package com.aboni.sensors;

import com.aboni.misc.Utils;
import com.aboni.utils.HWSettings;
import com.aboni.utils.ServerLog;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import java.io.IOException;

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
	protected void initSensor(int bus) throws IOException, UnsupportedBusNumberException {
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
     * @return Non-smoothed tilt-compensated compass heading
     */
    @Override
    public double getUnfilteredSensorHeading() {
        double[] magRaw = magnetometer.getMagVector();
        double[] accRaw = gyro.readRawAccel();
        return compass.getTiltCompensatedHeading(magRaw, accRaw);
    }

    public double[] getRotationDegrees() throws SensorNotInitializedException {
        double[] r = gyro.readAccel();
        return new double[]{Math.toDegrees(r[0]),
                Math.toDegrees(r[1]), Math.toDegrees(r[2])};
    }

    @Override
    protected void onLoadCompassConfiguration() {
        super.onLoadCompassConfiguration();
        updateCalibration();
    }

    private void updateCalibration() {
	    try {
	        int x = HWSettings.getPropertyAsInteger("calibration.x", 0);
	        int y = HWSettings.getPropertyAsInteger("calibration.y", 0);
	        int z = HWSettings.getPropertyAsInteger("calibration.z", 0);
	        compass.setCalibration(x, y, z);
        } catch (Exception e) {
            ServerLog.getLogger().error("Cannot load compass calibration!", e);
        }
    }

    @Override
    protected void onCompassRead() throws SensorException {
        gyro.read();
        magnetometer.read();
    }

    @Override
    public double getUnfilteredPitch() throws SensorNotInitializedException {
        double[] rot = getRotationDegrees();
        return rot[1];
    }

    @Override
    public double getUnfilteredRoll() throws SensorNotInitializedException {
        double[] rot = getRotationDegrees();
        return rot[0];
    }
}
