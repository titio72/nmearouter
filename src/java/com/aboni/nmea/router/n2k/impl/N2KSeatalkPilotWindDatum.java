package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

public class N2KSeatalkPilotWindDatum extends N2KMessageImpl {

    public static final int PGN = 65345;

    private double windDatum;
    private double rollingAverageWind;

    public N2KSeatalkPilotWindDatum(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KSeatalkPilotWindDatum(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        Double d = parseDouble(data, 16, 0, 16, 0.0001, false);
        windDatum = (d == null) ? Double.NaN : Utils.round(Math.toDegrees(d), 1);

        d = parseDouble(data, 32, 0, 16, 0.0001, false);
        rollingAverageWind = (d == null) ? Double.NaN : Utils.round(Math.toDegrees(d), 1);
    }

    public double getRollingAverageWind() {
        return rollingAverageWind;
    }

    public double getWindDatum() {
        return windDatum;
    }
}
