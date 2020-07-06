package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

public class N2KWindData extends N2KMessageImpl {

    private int SID;
    private double speed = Double.NaN;
    private double angle = Double.NaN;
    private boolean apparent = true;

    public N2KWindData(byte[] data) {
        super(getDefaultHeader(getInternalPgn()), data);
        fill();
    }

    public N2KWindData(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != getInternalPgn())
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", getInternalPgn(), header.getPgn()));
        fill();
    }

    private void fill() {
        SID = getByte(data, 0, 0xFF);

        Double dSpeed = parseDouble(data, 8, 0, 16, 0.01, false);
        if (dSpeed != null) speed = Utils.round(dSpeed * 3600.0 / 1852.0, 2);

        Double dAngleRad = parseDouble(data, 24, 0, 16, 0.0001, false);
        if (dAngleRad != null) angle = Utils.round(Math.toDegrees(dAngleRad), 1);

        apparent = (getByte(data, 5, 1) & 0x07) != 0;
    }

    private static int getInternalPgn() {
        return 130306;
    }

    public int getSID() {
        return SID;
    }

    public double getSpeed() {
        return speed;
    }

    public double getAngle() {
        return angle;
    }

    public boolean isApparent() {
        return apparent;
    }
}
