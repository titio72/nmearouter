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

public enum EngineStatus {
    UNKNOWN(2),
    ON(1),
    OFF(0);

    private final int value;

    EngineStatus(int i) {
        this.value = i;
    }

    public int getValue() {
        return value;
    }

    public static EngineStatus valueOf(int b) {
        switch (b) {
            case 0:
                return OFF;
            case 1:
                return ON;
            default:
                return UNKNOWN;
        }
    }

    public byte toByte() {
        return (byte) value;
    }

    @Override
    public String toString() {
        switch (value) {
            case 0: return "Off";
            case 1: return "On";
            default: return "Unknown";
        }
    }
}
