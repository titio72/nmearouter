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

public enum TemperatureSource {

    UNKNOWN(-1),
    SEA(0),
    OUTSIDE(1),
    INSIDE(2),
    ENGINE_ROOM(3),
    MAIN_CABIN_ROOM(4),
    LIVE_WELL(5),
    BAIT_WELL(6),
    REFRIGERATION(7),
    HEATING_SYSTEM(8),
    DEW_POINT(9),
    APPARENT_WIND_CHILL(10),
    THEORETICAL_WIND_CHILL(11),
    HEAT_INDEX(12),
    FREEZER(13),
    EXHAUST_GAS(14),
    CPU(32);

    private final int value;

    TemperatureSource(int value) {
        this.value = value;
    }

    public static TemperatureSource valueOf(int value) {
        switch (value) {
            case 0:
                return SEA;
            case 1:
                return OUTSIDE;
            case 2:
                return INSIDE;
            case 3:
                return ENGINE_ROOM;
            case 4:
                return MAIN_CABIN_ROOM;
            case 5:
                return LIVE_WELL;
            case 6:
                return BAIT_WELL;
            case 7:
                return REFRIGERATION;
            case 8:
                return HEATING_SYSTEM;
            case 9:
                return DEW_POINT;
            case 10:
                return APPARENT_WIND_CHILL;
            case 11:
                return THEORETICAL_WIND_CHILL;
            case 12:
                return HEAT_INDEX;
            case 13:
                return FREEZER;
            case 14:
                return EXHAUST_GAS;
            case 32:
                return CPU;
            default:
                return UNKNOWN;

        }
    }

    @Override
    public String toString() {
        switch (value) {
            case 0:
                return "Sea Temperature";
            case 1:
                return "Outside Temperature";
            case 2:
                return "Inside Temperature";
            case 3:
                return "Engine Room Temperature";
            case 4:
                return "Main Cabin Temperature";
            case 5:
                return "Live Well Temperature";
            case 6:
                return "Bait Well Temperature";
            case 7:
                return "Refrigeration Temperature";
            case 8:
                return "Heating System Temperature";
            case 9:
                return "Dew Point Temperature";
            case 10:
                return "Apparent Wind Chill Temperature";
            case 11:
                return "Theoretical Wind Chill Temperature";
            case 12:
                return "Heat Index Temperature";
            case 13:
                return "Freezer Temperature";
            case 14:
                return "Exhaust Gas Temperature";
            case 32:
                return "CPU Temperature";
            default:
                return "Unknown";
        }
    }
}
