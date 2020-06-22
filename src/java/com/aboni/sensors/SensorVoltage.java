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
import com.aboni.sensors.hw.ADS1115;
import com.aboni.utils.HWSettings;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import java.io.IOException;

public class SensorVoltage extends I2CSensor {

    private static final double MULTIPLIER = 5.0;

    private ADS1115 ads;
    private final double[] v;
    private int address;
    private double smoothing;
    private final double[] adj;

    public SensorVoltage(int address) {
        super();
        ads = null;
        this.address = address;
        smoothing = 0.75;
        adj = new double[] { 1, 1, 1, 1 };
        v = new double[] {0.0, 0.0, 0.0, 0.0};
    }

    public SensorVoltage() {
        this(ADS1115.ADS1115_ADDRESS_0X48);

        String sAddress = HWSettings.getProperty("analog.voltage", "0x48");

        smoothing = getDefaultSmoothingAlpha();
        String sSmoothing = HWSettings.getProperty("analog.voltage.smoothing", "");
        if (sSmoothing != null && !sSmoothing.isEmpty()) {
            try {
                smoothing = Double.parseDouble(sSmoothing);
            } catch (Exception e) {
                error("Cannot parse voltage smoothing factor " + sSmoothing, e);
            }
        }

        loadAdjustment();

        if (sAddress.startsWith("0x")) {
            this.address = Integer.parseInt(sAddress.substring(2), 16);
        } else {
            this.address = Integer.parseInt(sAddress);
        }


    }

    private void loadAdjustment() {
        for (int i = 0; i<4; i++) {
            adj[i] = HWSettings.getPropertyAsDouble("analog.voltage.adjust." + i, 1.0);
        }
    }

    public double getVoltage0() {
        return v[0] * adj[0];
    }

    public double getVoltage1() {
        return v[1] * adj[1];
    }

    public double getVoltage2() {
        return v[2] * adj[2];
    }

    public double getVoltage3() {
        return v[3] * adj[3];
    }

    @Override
    protected void initSensor(int bus) throws SensorException {
        try {
            ads = new ADS1115(new I2CInterface(bus, address), MULTIPLIER);
        } catch (IOException | UnsupportedBusNumberException e) {
            throw new SensorException("Error initializing ADS1115", e);
        }
    }

    @Override
    public String getSensorName() {
        return "ADS1115-15V";
    }

    @Override
    protected void readSensor() throws SensorException {
        loadAdjustment();
        for (int i = 0; i<4; i++) {
            try {
                double voltage = ads.getVoltage(i);
                v[i] = DataFilter.getLPFReading(smoothing, v[i], voltage);
            } catch (IOException e) {
                throw new SensorException("Error reading voltage", e);
            }
        }
    }
}
