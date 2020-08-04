package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.WATER_REFERENCE;

public class N2KSpeed extends N2KMessageImpl {

    public static final int PGN = 128259;

    private int sid;
    private double speedWaterRef;
    private double speedGroundRef;
    private String waterRefType;
    private int speedDirection;

    public N2KSpeed(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KSpeed(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    private void fill() {

        sid = getByte(data, 0, 0);

        Double dSpeedW = parseDouble(data, 8, 0, 16, 0.01, false);
        speedWaterRef = dSpeedW == null ? Double.NaN : Utils.round(dSpeedW * 3600.0 / 1852.0, 2);

        Double dSpeedG = parseDouble(data, 24, 0, 16, 0.01, false);
        speedGroundRef = dSpeedG == null ? Double.NaN : Utils.round(dSpeedG * 3600.0 / 1852.0, 2);

        waterRefType = parseEnum(data, 40, 0, 8, N2KLookupTables.getTable(WATER_REFERENCE));

        Long lSpeedDir = parseInteger(data, 48, 0, 4, false);
        if (lSpeedDir != null) speedDirection = lSpeedDir.intValue();
    }

    public int getSID() {
        return sid;
    }

    public double getSpeedWaterRef() {
        return speedWaterRef;
    }

    public double getSpeedGroundRef() {
        return speedGroundRef;
    }

    public String getWaterRefType() {
        return waterRefType;
    }

    public int getSpeedDirection() {
        return speedDirection;
    }
}
