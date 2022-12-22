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

import com.aboni.misc.DataFilter;
import com.aboni.misc.Utils;
import com.aboni.sensors.hw.HMC5883L;
import com.aboni.utils.Log;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.IOException;

public class SensorHMC5883 extends I2CSensor {

    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;

    private HMC5883L hmc5883l;
    private double[] mag;

    @Inject
    public SensorHMC5883(@NotNull Log log) {
        super(log);
        setDefaultSmoothingAlpha(0.66);
    }

    @Override
    protected void initSensor(int bus) throws SensorException {
        try {
            hmc5883l = new HMC5883L(new I2CInterface(bus, HMC5883L.HMC5883_I2C_ADDRESS));
            int i = 0;
            while (i < 3 && !doInit()) {
                i++;
                Utils.pause(5000);
            }
        } catch (IOException | UnsupportedBusNumberException e) {
            throw new SensorException("Error initializing HCM5883", e);
        }
    }

    private boolean doInit() {
        try {
            Thread.sleep(500);
            hmc5883l.enable();
            Thread.sleep(500);
            hmc5883l.setContinuousMode();
            Thread.sleep(500);
            hmc5883l.setScale(HMC5883L.Scale.Gauss_1_30);
            return true;
        } catch (InterruptedException e) {
            error("Failed initialization HMC5883L", e);
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            error("Failed initialization HMC5883L", e);
            return false;
        }
    }

    @Override
    protected void readSensor() throws SensorException {
        synchronized (this) {
            if (hmc5883l != null) {
                try {
                    setMagReading(hmc5883l.getScaledMag());
                } catch (IOException e) {
                    throw new SensorException("Error reading compass", e);
                }
            }
        }
    }

    private void setMagReading(double[] m) {
        if (mag==null) {
            mag = m;
        } else {
            mag = new double[]{
                    DataFilter.getLPFReading(getDefaultSmoothingAlpha(), mag[X], m[X]),
                    DataFilter.getLPFReading(getDefaultSmoothingAlpha(), mag[Y], m[Y]),
                    DataFilter.getLPFReading(getDefaultSmoothingAlpha(), mag[Z], m[Z])
            };
        }
    }

    public double[] getMagVector() {
        return mag;
    }

    @Override
    public String getSensorName() {
        return "HMC5883";
    }
}
