package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

public class N2KRateOfTurn extends N2KMessageImpl {

    public static final int PGN = 127251;

    private int sid;
    private double rate;

    public N2KRateOfTurn(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KRateOfTurn(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        sid = getByte(data, 0, 0xFF);

        Double dRate = parseDouble(data, 8, 0, 32, 3.125e-08, true);
        rate = dRate == null ? Double.NaN : Utils.round(Math.toDegrees(dRate), 4);
    }

    public int getSID() {
        return sid;
    }

    public double getRate() {
        return rate;
    }
}
