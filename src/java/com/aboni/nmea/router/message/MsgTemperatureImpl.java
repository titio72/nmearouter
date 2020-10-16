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

package com.aboni.nmea.router.message;

public class MsgTemperatureImpl implements MsgTemperature {

    private final double temperature;
    private final TemperatureSource src;

    public MsgTemperatureImpl(TemperatureSource src, double temperature) {
        this.src = src;
        this.temperature = temperature;
    }

    @Override
    public int getSID() {
        return -1;
    }

    @Override
    public int getInstance() {
        return 0;
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
        return 0;
    }

    @Override
    public String toString() {
        return String.format("Temperature: Source {%s} Temperature {%.1f}", getTemperatureSource(), getTemperature());
    }
}
