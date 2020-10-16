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

public enum PressureSource {

    UNKNOWN(-1),
    ATMOSPHERIC(0),
    WATER(1),
    STEAM(2),
    COMPRESSED_AIR(3),
    HYDRAULIC(4);

    private final int value;

    PressureSource(int v) {
        value = v;
    }

    public static PressureSource valueOf(int v) {
        switch (v) {
            case 0:
                return ATMOSPHERIC;
            case 1:
                return WATER;
            case 2:
                return STEAM;
            case 3:
                return COMPRESSED_AIR;
            case 4:
                return HYDRAULIC;
            default:
                return UNKNOWN;
        }
    }

    @Override
    public String toString() {
        switch (value) {
            case 0:
                return "Atmospheric";
            case 1:
                return "Water";
            case 2:
                return "Steam";
            case 3:
                return "Compressed Air";
            case 4:
                return "Hydraulic";
            default:
                return "Unknown";
        }
    }
}
