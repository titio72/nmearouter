package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

public class N2KEnvironment310 extends N2KMessageImpl {

    private int sid;
    private double waterTemp;
    private double airTemp;
    private double atmosphericPressure;


    public N2KEnvironment310(byte[] data) {
        super(getDefaultHeader(getInternalPgn()), data);
        fill();
    }

    public N2KEnvironment310(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != getInternalPgn())
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", getInternalPgn(), header.getPgn()));
        fill();
    }

    private void fill() {
        sid = getByte(data, 0, 0xFF);

        Double dWT = parseDouble(data, 8, 0, 16, 0.01, false);
        waterTemp = (dWT == null) ? Double.NaN : Utils.round(dWT - 273.15, 1);

        Double dAT = parseDouble(data, 24, 0, 16, 0.01, false);
        airTemp = (dAT == null) ? Double.NaN : Utils.round(dAT - 273.15, 1);

        Long dP = parseInteger(data, 40, 0, 16, false);
        atmosphericPressure = (dP == null) ? Double.NaN : Utils.round(dP / 100, 1);

    }

    private static int getInternalPgn() {
        return 130310;
    }

    public int getSID() {
        return sid;
    }

    public double getWaterTemp() {
        return waterTemp;
    }

    public double getAirTemp() {
        return airTemp;
    }

    public double getAtmosphericPressure() {
        return atmosphericPressure;
    }
}
