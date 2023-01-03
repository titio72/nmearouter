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
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import javax.inject.Inject;
import java.io.IOException;

public class CMPS11CompassDataProvider implements CompassDataProvider {

    private I2CInterface device;
    private final int bus;

    @Inject
    public CMPS11CompassDataProvider() {
        this.bus = HWSettings.getPropertyAsInteger("bus", 1);
    }

    private static final int ADDRESS = 0x60;
    private double heading;
    private double pitch;
    private double roll;
    private boolean init;

    @Override
    public void init() throws SensorException {
        synchronized (this) {
            try {
                if (!init) {
                    device = new I2CInterface(bus, ADDRESS);
                    init = true;
                }
            } catch (IOException | UnsupportedBusNumberException e) {
                throw new SensorException("Cannot initialize CMPS11", e);
            }
        }
    }

    @Override
    public void refreshConfiguration() {
        // nothing to do here
    }

    @Override
    public double[] read() throws SensorException {
        if (init) {
            readBus();
            return new double[]{pitch, roll, heading};
        } else {
            throw new SensorNotInitializedException("CMPS11 not initialized");
        }
    }

    protected void readBus() throws SensorException {
        try {
            int b1 = device.readU8(3);
            int b2 = device.readU8(2) * 256;
            int h255 = (b1 & 0xFF) + b2;
            heading = h255 / 10.0;

            roll = device.readS8(5);
            pitch = device.readS8(4);
        } catch (IOException e) {
            throw new SensorException("Error reading CMPS11 data", e);
        }
    }
}
