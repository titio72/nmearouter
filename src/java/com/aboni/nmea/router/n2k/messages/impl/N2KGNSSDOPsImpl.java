package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.nmea.router.message.GNSSFix;
import com.aboni.nmea.router.message.MsgGNSSDOPs;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.GNSS_DOP_PGN;

public class N2KGNSSDOPsImpl extends N2KMessageImpl implements MsgGNSSDOPs {

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
        sid = BitUtils.getByte(data, 0, 0xFF);
        fix = of((int) BitUtils.parseIntegerSafe(data, 11, 3, 3, 6));
        hDOP = BitUtils.parseDoubleSafe(data, 16, 16, 0.01, true);
        vDOP = BitUtils.parseDoubleSafe(data, 32, 16, 0.01, true);
        tDOP = BitUtils.parseDoubleSafe(data, 48, 16, 0.01, true);
    }

    @Override
    public int getSID() {
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