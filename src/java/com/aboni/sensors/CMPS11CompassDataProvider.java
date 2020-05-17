package com.aboni.sensors;

import com.aboni.utils.HWSettings;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import java.io.IOException;

public class CMPS11CompassDataProvider implements CompassDataProvider {

    private I2CInterface device;
    private final int bus;

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
            heading = ((double) h255 / 10.0);

            roll = device.readS8(5);
            pitch = device.readS8(4);
        } catch (IOException e) {
            throw new SensorException("Error reading CMPS11 data", e);
        }
	}
}
