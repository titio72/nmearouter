package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

public class N2KSeatalkPilotLockedHeading extends N2KMessageImpl {

    public static final int PGN = 65360;

    private double lockedHeadingTrue;
    private double lockedHeadingMagnetic;

    public N2KSeatalkPilotLockedHeading(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    public N2KSeatalkPilotLockedHeading(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    private void fill() {
        Double d = parseDouble(data, 40, 0, 16, 0.0001, false);
        lockedHeadingMagnetic = (d == null) ? Double.NaN : Utils.round(Math.toDegrees(d), 1);

        d = parseDouble(data, 24, 0, 16, 0.0001, false);
        lockedHeadingTrue = (d == null) ? Double.NaN : Utils.round(Math.toDegrees(d), 1);
    }

    public double getLockedHeadingMagnetic() {
        return lockedHeadingMagnetic;
    }

    public double getLockedHeadingTrue() {
        return lockedHeadingTrue;
    }
}
