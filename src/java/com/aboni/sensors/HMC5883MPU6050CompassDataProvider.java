package com.aboni.sensors;

import com.aboni.misc.Utils;
import com.aboni.utils.HWSettings;
import com.aboni.utils.ServerLog;

public class HMC5883MPU6050CompassDataProvider implements CompassDataProvider {

    private final SensorHMC5883 magnetometer;
    private final SensorMPU6050 gyro;
    private final MagnetometerToCompass compass;
    private final int bus;
    private boolean init;

    public HMC5883MPU6050CompassDataProvider() {
        this.bus = HWSettings.getPropertyAsInteger("bus", 1);
        this.init = false;
        gyro = new SensorMPU6050();
        magnetometer = new SensorHMC5883();
        compass = new MagnetometerToCompass();
    }

    @Override
    public final void init() throws SensorException {
        synchronized (this) {
            if (!init) {
                // initialize gyro first
                gyro.init(bus);
                // not clear if it's necessary
                Utils.pause(500);
                // if the gyro hasn't started the magnetometer will fail to initialize
                magnetometer.init(bus);

                init = true;
            }
        }
    }

    @Override
    public void refreshConfiguration() {
        updateCalibration();
    }

    @Override
    public double[] read() throws SensorException {
        synchronized (this) {
            if (init) {
                gyro.read();
                magnetometer.read();

                double[] magRaw = magnetometer.getMagVector();
                double[] accRaw = gyro.readRawAccel();
                double head = compass.getTiltCompensatedHeading(magRaw, accRaw);
                double[] rot = getRotationDegrees();
                double roll = rot[0];
                double pitch = rot[0];

                return new double[]{pitch, roll, head};
            } else {
                throw new SensorNotInitializedException("HMC5883&MPU6050 CompassDataProvider not initialized");
            }
        }
    }

    private double[] getRotationDegrees() throws SensorNotInitializedException {
        double[] r = gyro.readAccel();
        return new double[]{Math.toDegrees(r[0]),
                Math.toDegrees(r[1]), Math.toDegrees(r[2])};
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
}
