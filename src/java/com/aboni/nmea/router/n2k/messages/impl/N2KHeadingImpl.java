package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.message.DirectionReference;
import com.aboni.nmea.router.message.MsgHeading;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.HEADING_PGN;

public class N2KHeadingImpl extends N2KMessageImpl implements MsgHeading {

    private int sid;
    private double heading;
    private double deviation;
    private double variation;
    private DirectionReference reference;

    public N2KHeadingImpl(byte[] data) {
        super(getDefaultHeader(HEADING_PGN), data);
        fill();
    }

    public N2KHeadingImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != HEADING_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", HEADING_PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        sid = getByte(data, 0, 0xFF);

        Double dH = parseDouble(data, 8, 16, 0.0001, false);
        heading = dH == null ? Double.NaN : Utils.round(Math.toDegrees(dH), 1);

        Double dD = parseDouble(data, 24, 16, 0.0001, true);
        deviation = dD == null ? Double.NaN : Utils.round(Math.toDegrees(dD), 1);

        Double dV = parseDouble(data, 40, 16, 0.0001, true);
        variation = dV == null ? Double.NaN : Utils.round(Math.toDegrees(dV), 1);

        reference = DirectionReference.valueOf((int) parseIntegerSafe(data, 56, 0, 2, 0));
    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public double getHeading() {
        return heading;
    }

    @Override
    public double getDeviation() {
        return deviation;
    }

    @Override
    public double getVariation() {
        return variation;
    }

    @Override
    public DirectionReference getReference() {
        return reference;
    }

    @Override
    public boolean isTrueHeading() {
        return DirectionReference.TRUE == reference;
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Heading {%.1f} Ref {%s}",
                HEADING_PGN, getHeader().getSource(), getHeading(), getReference());
    }
}
