package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

public class N2KRudder extends N2KMessageImpl {

    private int instance;
    private double position;
    private double angleOrder;
    private int directionOrder;

    public N2KRudder(byte[] data) {
        super(getDefaultHeader(getInternalPgn()), data);
        fill();
    }

    protected N2KRudder(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != getInternalPgn())
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", getInternalPgn(), header.getPgn()));
        fill();
    }

    private void fill() {
        instance = getByte(data, 0, 0xFF);

        Long i = parseInteger(data, 8, 0, 2, false);
        directionOrder = i == null ? -1 : i.intValue();

        Double dAO = parseDouble(data, 16, 0, 16, 0.0001, true);
        angleOrder = dAO == null ? Double.NaN : Utils.round(Math.toDegrees(dAO), 1);

        Double dP = parseDouble(data, 32, 0, 16, 0.0001, true);
        position = dP == null ? Double.NaN : Utils.round(Math.toDegrees(dP), 1);
    }

    public int getInstance() {
        return instance;
    }

    public double getPosition() {
        return position;
    }

    public double getAngleOrder() {
        return angleOrder;
    }

    public int getDirectionOrder() {
        return directionOrder;
    }

    private static int getInternalPgn() {
        return 127245;
    }
}
