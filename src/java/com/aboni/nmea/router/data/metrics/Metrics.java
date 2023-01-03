/*
 * Copyright (c) 2021,  Andrea Boni
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

package com.aboni.nmea.router.data.metrics;

import com.aboni.nmea.router.data.Unit;

public final class Metrics {

    private Metrics() {
    }

    public static final Metric PRESSURE = new Metric("P_ATM", "Atmospheric pressure", Unit.MILLIBAR);
    public static final Metric AIR_TEMPERATURE = new Metric("T_AIR", "Air temperature", Unit.CELSIUS);
    public static final Metric WATER_TEMPERATURE = new Metric("T_WTR", "Water temperature", Unit.CELSIUS);
    public static final Metric HUMIDITY = new Metric("HUM", "Relative humidity", Unit.PERCENTAGE);
    public static final Metric WIND_SPEED = new Metric("W_SP", "Wind speed", Unit.KNOTS);
    public static final Metric WIND_DIRECTION = new Metric("W_DR", "Wind direction", Unit.DEGREES);
    public static final Metric ROLL = new Metric("ROLL", "Boat roll", Unit.DEGREES_SCALAR);
}
