package com.aboni.nmea.router.n2k;

public class SeatalkPilotMode {

    public enum Mode {
        STANDBY,
        AUTO,
        VANE,
        TRACK,
        TRACK_DEV,
        UNKNOWN
    }

    private int mode = -1;
    private int subMode = -1;

    public SeatalkPilotMode(int m, int s) {
        setMode(m);
        setSubMode(s);
    }

    public void setPilotMode(Mode m) {
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

    public Mode getPilotMode() {
        if (mode == 0 && subMode == 0) return Mode.STANDBY;
        else if (mode == 64 && subMode == 0) return Mode.AUTO;
        else if (mode == 0 && subMode == 1) return Mode.VANE;
        else if (mode == 128 && subMode == 1) return Mode.TRACK;
        else if (mode == 129 && subMode == 1) return Mode.TRACK_DEV;
        else return Mode.UNKNOWN;
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
}
