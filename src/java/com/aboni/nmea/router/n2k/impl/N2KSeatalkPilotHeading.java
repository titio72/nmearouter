package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KMessageHeader;

public class N2KSeatalkPilotHeading extends N2KMessageImpl {

    public static final int PGN = 65359;

    private double headingMagnetic;

    private double headingTrue;

    public N2KSeatalkPilotHeading(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KSeatalkPilotHeading(N2KMessageHeader header, byte[] data) {
        super(header, data);
        fill();
    }

    private void fill() {
        Double d = parseDouble(data, 40, 0, 16, 0.0001, false);
        headingMagnetic = (d == null) ? Double.NaN : Utils.round(Math.toDegrees(d), 1);

        d = parseDouble(data, 24, 0, 16, 0.0001, false);
        headingTrue = (d == null) ? Double.NaN : Utils.round(Math.toDegrees(d), 1);
    }

    public double getHeadingMagnetic() {
        return headingMagnetic;
    }

    public double getHeadingTrue() {
        return headingTrue;
    }
}
