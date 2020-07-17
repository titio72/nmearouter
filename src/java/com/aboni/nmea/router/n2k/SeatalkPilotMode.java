package com.aboni.nmea.router.n2k;

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
