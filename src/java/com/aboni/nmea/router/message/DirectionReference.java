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

public enum DirectionReference {

    UNKNOWN(-1),
    TRUE(0),
    MAGNETIC(1),
    ERROR(2),
    NULL(3);

    private final int value;

    DirectionReference(int v) {
        value = v;
    }

    public static DirectionReference valueOf(int v) {
        switch (v) {
            case 0:
                return TRUE;
            case 1:
                return MAGNETIC;
            case 2:
                return ERROR;
            case 3:
                return NULL;
            default:
                return UNKNOWN;
        }
    }

    @Override
    public String toString() {
        switch (value) {
            case 0:
                return "True";
            case 1:
                return "Magnetic";
            case 2:
                return "Error";
            case 3:
                return "Null";
            default:
                return "Unknown";
        }
    }
}
