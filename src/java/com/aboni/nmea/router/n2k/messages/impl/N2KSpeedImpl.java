package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.messages.N2KSpeed;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.WATER_REFERENCE;
import static com.aboni.nmea.router.n2k.messages.N2kMessagePGNs.SPEED_PGN;

public class N2KSpeedImpl extends N2KMessageImpl implements N2KSpeed {

    private int sid;
    private double speedWaterRef;
    private double speedGroundRef;
    private String waterRefType;
    private int speedDirection;

    public N2KSpeedImpl(byte[] data) {
        super(getDefaultHeader(SPEED_PGN), data);
        fill();
    }

    public N2KSpeedImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != SPEED_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", SPEED_PGN, header.getPgn()));
        fill();
    }

    private void fill() {

        sid = getByte(data, 0, 0);

        Double dSpeedW = parseDouble(data, 8, 16, 0.01, false);
        speedWaterRef = dSpeedW == null ? Double.NaN : Utils.round(dSpeedW * 3600.0 / 1852.0, 2);

        Double dSpeedG = parseDouble(data, 24, 16, 0.01, false);
        speedGroundRef = dSpeedG == null ? Double.NaN : Utils.round(dSpeedG * 3600.0 / 1852.0, 2);

        waterRefType = parseEnum(data, 40, 0, 8, N2KLookupTables.getTable(WATER_REFERENCE));

        Long lSpeedDir = parseInteger(data, 48, 4);
        if (lSpeedDir != null) speedDirection = lSpeedDir.intValue();
    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public double getSpeedWaterRef() {
        return speedWaterRef;
    }

    @Override
    public double getSpeedGroundRef() {
        return speedGroundRef;
    }

    @Override
    public String getWaterRefType() {
        return waterRefType;
    }

    @Override
    public int getSpeedDirection() {
        return speedDirection;
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Water Speed {%.1f}",
                SPEED_PGN, getHeader().getSource(), getSpeedWaterRef());
    }
}
