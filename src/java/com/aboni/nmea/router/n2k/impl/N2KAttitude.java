package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

public class N2KAttitude extends N2KMessageImpl {

    private static final int PGN = 127257;

    private int sid;
    private double yaw;
    private double pitch;
    private double roll;

    public N2KAttitude(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KAttitude(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        sid = getByte(data, 0, 0xFF);

        Double dYaw = parseDouble(data, 8, 0, 16, 0.0001, true);
        yaw = (dYaw == null) ? Double.NaN : Utils.round(Math.toDegrees(dYaw), 1);

        Double dPitch = parseDouble(data, 24, 0, 16, 0.0001, true);
        pitch = (dPitch == null) ? Double.NaN : Utils.round(Math.toDegrees(dPitch), 1);

        Double dRoll = parseDouble(data, 40, 0, 16, 0.0001, true);
        roll = (dRoll == null) ? Double.NaN : Utils.round(Math.toDegrees(dRoll), 1);
    }

    public int getSID() {
        return sid;
    }

    public double getYaw() {
        return yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public double getRoll() {
        return roll;
    }

}
