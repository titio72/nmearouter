package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.DIRECTION_REFERENCE;

public class N2KHeading extends N2KMessageImpl {

    public static final int PGN = 127250;

    private int sid;
    private double heading;
    private double deviation;
    private double variation;
    private String reference;

    public N2KHeading(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KHeading(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        sid = getByte(data, 0, 0xFF);

        Double dH = parseDouble(data, 8, 0, 16, 0.0001, false);
        heading = dH == null ? Double.NaN : Utils.round(Math.toDegrees(dH), 1);

        Double dD = parseDouble(data, 24, 0, 16, 0.0001, true);
        deviation = dD == null ? Double.NaN : Utils.round(Math.toDegrees(dD), 1);

        Double dV = parseDouble(data, 40, 0, 16, 0.0001, true);
        variation = dV == null ? Double.NaN : Utils.round(Math.toDegrees(dV), 1);

        reference = parseEnum(data, 56, 0, 2, N2KLookupTables.getTable(DIRECTION_REFERENCE));
    }

    public int getSID() {
        return sid;
    }

    public double getHeading() {
        return heading;
    }

    public double getDeviation() {
        return deviation;
    }

    public double getVariation() {
        return variation;
    }

    public String getReference() {
        return reference;
    }

    public boolean isTrueHeading() {
        return "True".equals(reference);
    }

}
