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

package com.aboni.nmea.router.data.meteo;

public enum MeteoMetrics {

    PRESSURE(0),
    AIR_TEMPERATURE(1),
    WATER_TEMPERATURE(2),
    HUMIDITY(3),
    WIND_SPEED(4),
    WIND_DIRECTION(5);

    private final int ix;

    public int getIx() {
        return ix;
    }

    MeteoMetrics(int ix) {
        this.ix = ix;
    }

    public static final int SIZE = 6;

    public static MeteoMetrics valueOf(int ix) {
        switch (ix) {
            case 0:
                return PRESSURE;
            case 1:
                return AIR_TEMPERATURE;
            case 2:
                return WATER_TEMPERATURE;
            case 3:
                return HUMIDITY;
            case 4:
                return WIND_SPEED;
            case 5:
                return WIND_DIRECTION;
            default:
                return null;
        }
    }
}
