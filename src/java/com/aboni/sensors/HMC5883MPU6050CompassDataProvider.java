/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.sensors;

import com.aboni.nmea.router.utils.HWSettings;
import com.aboni.nmea.router.utils.Log;
import com.aboni.utils.LogStringBuilder;
import com.aboni.utils.Utils;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class HMC5883MPU6050CompassDataProvider implements CompassDataProvider {

    private final SensorHMC5883 magnetometer;
    private final SensorMPU6050 gyro;
    private final MagnetometerToCompass compass;
    private final Log log;
    private final int bus;
    private boolean init;

    @Inject
    public HMC5883MPU6050CompassDataProvider(@NotNull Log log) {
        this.log = log;
        this.bus = HWSettings.getPropertyAsInteger("bus", 1);
        this.init = false;
        gyro = new SensorMPU6050(log);
        magnetometer = new SensorHMC5883(log);
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
            log.error(LogStringBuilder.start("CompassDataProvider").wO("load calibration").toString(), e);
        }
    }
}
