/*
 * Copyright (c) 2020,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.message.impl;

import com.aboni.nmea.router.message.MsgBattery;

public class MsgBatteryImpl implements MsgBattery {

    private final int instance;
    private final int sid;
    private final double voltage;
    private final double current;
    private final double temperature;

    public MsgBatteryImpl(int instance, double voltage) {
        this(0, instance, voltage, Double.NaN, Double.NaN);
    }

    public MsgBatteryImpl(int instance, double voltage, double current) {
        this(0, instance, voltage, current, Double.NaN);
    }

    public MsgBatteryImpl(int sid, int instance, double voltage, double current, double temperature) {
        this.instance = instance;
        this.sid = sid;
        this.voltage = voltage;
        this.current = current;
        this.temperature = temperature;
    }

    @Override
    public int getInstance() {
        return instance;
    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public double getVoltage() {
        return voltage;
    }

    @Override
    public double getCurrent() {
        return current;
    }

    @Override
    public double getTemperature() {
        return temperature;
    }

    @Override
    public String toString() {
        return String.format("Battery: Instance {%d} Voltage {%.1f} Current {%.1f} Temperature {%.1f} ",
                getInstance(), getVoltage(), getCurrent(), getTemperature());
    }
}
