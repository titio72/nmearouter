package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

public class N2KRateOfTurn extends N2KMessageImpl {

    private int sid;
    private double rate;


    public N2KRateOfTurn(byte[] data) {
        super(getDefaultHeader(getInternalPgn()), data);
        fill();
    }

    public N2KRateOfTurn(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != getInternalPgn())
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", getInternalPgn(), header.getPgn()));
        fill();
    }

    private void fill() {
        sid = getByte(data, 0, 0xFF);

        Double dRate = parseDouble(data, 8, 0, 32, 3.125e-08, true);
        rate = dRate == null ? Double.NaN : Utils.round(Math.toDegrees(dRate), 4);
    }

    private static int getInternalPgn() {
        return 127251;
    }

    public int getSID() {
        return sid;
    }

    public double getRate() {
        return rate;
    }
}
