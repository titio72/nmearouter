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

public class MsgPressureImpl implements MsgPressure {

    private final double pressure;
    private final PressureSource src;

    public MsgPressureImpl(PressureSource src, double pressure) {
        this.src = src;
        this.pressure = pressure;
    }

    @Override
    public int getSID() {
        return -1;
    }

    @Override
    public PressureSource getPressureSource() {
        return src;
    }

    @Override
    public double getPressure() {
        return pressure;
    }

    @Override
    public String toString() {
        return String.format("Pressure: Source {%s} Pressure {%.1f}", getPressureSource(), getPressure());
    }
}
