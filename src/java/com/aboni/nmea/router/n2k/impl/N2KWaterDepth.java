package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

public class N2KWaterDepth extends N2KMessageImpl {

    public static final int PGN = 128267;

    private int sid;
    private double depth;
    private double offset;
    private double range;

    public N2KWaterDepth(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KWaterDepth(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        sid = getByte(data, 0, 0);

        Double dDepth = parseDouble(data, 8, 0, 32, 0.01, false);
        depth = dDepth == null ? Double.NaN : dDepth;

        Double dOffset = parseDouble(data, 40, 0, 8, 0.001, false);
        offset = dOffset == null ? Double.NaN : dOffset;

        Double dRange = parseDouble(data, 56, 0, 8, 10, false);
        range = dRange == null ? Double.NaN : dRange;
    }

    public int getSID() {
        return sid;
    }

    public double getDepth() {
        return depth;
    }

    public double getOffset() {
        return offset;
    }

    public double getRange() {
        return range;
    }
}
