package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

public class N2KEnvironment311 extends N2KMessageImpl {

    public static final int PGN = 130311;

    private String humiditySource;
    private String tempSource;
    private int sid;
    private double temperature;
    private double humidity;
    private double atmosphericPressure;


    public N2KEnvironment311(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KEnvironment311(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    private void fill() {

        sid = getByte(data, 0, 0xFF);

        tempSource = parseEnum(data, 8, 0, 6, N2KLookupTables.LOOKUP_TEMPERATURE_SOURCE);

        humiditySource = parseEnum(data, 14, 6, 2, N2KLookupTables.LOOKUP_HUMIDITY_SOURCE);

        Double dT = parseDouble(data, 16, 0, 16, 0.01, false);
        temperature = (dT == null) ? Double.NaN : Utils.round(dT - 273.15, 1);

        Double dH = parseDouble(data, 32, 0, 16, 0.004, true);
        humidity = (dH == null) ? Double.NaN : dH;

        Long dP = parseInteger(data, 48, 0, 16, false);
        atmosphericPressure = (dP == null) ? Double.NaN : dP;

    }

    public int getSID() {
        return sid;
    }

    public String getHumiditySource() {
        return humiditySource;
    }

    public String getTempSource() {
        return tempSource;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getAtmosphericPressure() {
        return atmosphericPressure;
    }
}
