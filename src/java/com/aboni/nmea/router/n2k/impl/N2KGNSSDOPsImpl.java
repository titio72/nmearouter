package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.GNSSFix;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.messages.N2KGNSSDOPs;

import static com.aboni.nmea.router.n2k.messages.N2kMessagePGNs.GNSS_DOP_PGN;

public class N2KGNSSDOPsImpl extends N2KMessageImpl implements N2KGNSSDOPs {

    private static GNSSFix of(int i) {
        switch (i) {
            case 0:
                return GNSSFix.FIX_1D;
            case 1:
                return GNSSFix.FIX_2D;
            case 2:
                return GNSSFix.FIX_3D;
            case 3:
                return GNSSFix.FIX_AUTO;
            case 7:
                return GNSSFix.FIX_UNAVAILABLE;
            default:
                return GNSSFix.FIX_ERROR;
        }
    }

    private int sid;
    private double hDOP;
    private double vDOP;
    private double tDOP;
    private GNSSFix fix = GNSSFix.FIX_ERROR;

    public N2KGNSSDOPsImpl(byte[] data) {
        super(getDefaultHeader(GNSS_DOP_PGN), data);
        fill();
    }

    public N2KGNSSDOPsImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != GNSS_DOP_PGN)
            throw new PGNDataParseException(String.format("Incompatible DOPs: expected %d, received %d", GNSS_DOP_PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        sid = getByte(data, 0, 0xFF);
        fix = of((int) parseIntegerSafe(data, 11, 3, 3, 6));
        hDOP = parseDoubleSafe(data, 16, 16, 0.01, true);
        vDOP = parseDoubleSafe(data, 32, 16, 0.01, true);
        tDOP = parseDoubleSafe(data, 48, 16, 0.01, true);
    }

    @Override
    public int getSid() {
        return sid;
    }

    @Override
    public double getHDOP() {
        return hDOP;
    }

    @Override
    public double getVDOP() {
        return vDOP;
    }

    @Override
    public double getTDOP() {
        return tDOP;
    }

    @Override
    public GNSSFix getFix() {
        return fix;
    }

    @Override
    public String getFixDescription() {
        return fix.toString();
    }


    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Fix {%s} HDOP {%.1f} VDOP {%.1f} TDOP {%.1f}",
                GNSS_DOP_PGN, getHeader().getSource(), getFixDescription(),
                getHDOP(), getVDOP(), getTDOP());
    }
}