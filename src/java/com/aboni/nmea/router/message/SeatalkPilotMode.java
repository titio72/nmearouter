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

package com.aboni.nmea.router.message;

public class SeatalkPilotMode {

    private int mode = 0xFF;
    private int subMode = 0xFF;
    private int data = 0xFF;

    public SeatalkPilotMode(int m, int s) {
        setMode(m);
        setSubMode(s);
    }

    public SeatalkPilotMode(int m, int s, int d) {
        setMode(m);
        setSubMode(s);
        setData(d);
    }

    public void setPilotMode(PilotMode m) {
        switch (m) {
            case STANDBY:
                mode = 0;
                subMode = 0;
                break;
            case AUTO:
                mode = 64;
                subMode = 0;
                break;
            case VANE:
                mode = 0;
                subMode = 1;
                break;
            case TRACK:
                mode = 128;
                subMode = 1;
                break;
            case TRACK_DEV:
                mode = 129;
                subMode = 1;
                break;
            default:
        }
    }

    public PilotMode getPilotMode() {
        if (mode == 0 && subMode == 0) return PilotMode.STANDBY;
        else if (mode == 64 && subMode == 0) return PilotMode.AUTO;
        else if (mode == 0 && subMode == 1) return PilotMode.VANE;
        else if (mode == 128 && subMode == 1) return PilotMode.TRACK;
        else if (mode == 129 && subMode == 1) return PilotMode.TRACK_DEV;
        else return PilotMode.UNKNOWN;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int m) {
        mode = m;
    }

    public int getSubMode() {
        return subMode;
    }

    public void setSubMode(int m) {
        subMode = m;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }
}
