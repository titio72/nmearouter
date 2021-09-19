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

import com.aboni.nmea.router.message.MsgTemperature;
import com.aboni.nmea.router.message.TemperatureSource;

public class MsgTemperatureImpl implements MsgTemperature {

    private final int instance;
    private final double temperature;
    private final double setTemperature;
    private final TemperatureSource src;
    private final int sid;

    public MsgTemperatureImpl(TemperatureSource src, double temperature) {
        this(-1, 0, src, temperature, Double.NaN);
    }

    public MsgTemperatureImpl(int sid, int instance, TemperatureSource src, double temperature, double setTemperature) {
        this.sid = sid;
        this.instance = instance;
        this.src = src;
        this.temperature = temperature;
        this.setTemperature = setTemperature;
    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public int getInstance() {
        return instance;
    }

    @Override
    public TemperatureSource getTemperatureSource() {
        return src;
    }

    @Override
    public double getTemperature() {
        return temperature;
    }

    @Override
    public double getSetTemperature() {
        return setTemperature;
    }

    @Override
    public String toString() {
        return String.format("Temperature: Source {%s} Temperature {%.1f}", getTemperatureSource(), getTemperature());
    }
}
