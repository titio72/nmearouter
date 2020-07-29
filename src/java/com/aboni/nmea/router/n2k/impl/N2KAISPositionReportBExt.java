package com.aboni.nmea.router.n2k.impl;


import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

public class N2KAISPositionReportBExt extends N2KAISPositionReportB {

    public static final int PGN = 129040;

    public N2KAISPositionReportBExt(byte[] data) throws PGNDataParseException {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KAISPositionReportBExt(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }
}