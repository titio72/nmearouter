package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.PilotMode;
import com.aboni.nmea.router.n2k.SeatalkPilotMode;

public class N2KSeatalkPilotMode extends N2KMessageImpl {

    public static final int PGN = 65379;

    private SeatalkPilotMode mode;

    public N2KSeatalkPilotMode(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KSeatalkPilotMode(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        int m = (int) parseIntegerSafe(data, 16, 0, 8, false, 0xFF);
        int sm = (int) parseIntegerSafe(data, 16, 0, 8, false, 0xFF);
        int d = (int) parseIntegerSafe(data, 16, 0, 8, false, 0xFF);
        mode.setData(d);
        mode.setSubMode(sm);
        mode.setMode(m);
    }

    public PilotMode getMode() {
        return mode.getPilotMode();
    }

    public void getMode(PilotMode m) {
        mode.setPilotMode(m);
    }
}
