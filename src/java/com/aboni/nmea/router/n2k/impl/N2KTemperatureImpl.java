package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.messages.N2KTemperature;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.TEMPERATURE_SOURCE;
import static com.aboni.nmea.router.n2k.messages.N2kMessagePGNs.ENVIRONMENT_TEMPERATURE_PGN;

public class N2KTemperatureImpl extends N2KMessageImpl implements N2KTemperature {

    private String source;
    private int sid;
    private int instance;
    private double temperature;
    private double setTemperature;


    public N2KTemperatureImpl(byte[] data) {
        super(getDefaultHeader(ENVIRONMENT_TEMPERATURE_PGN), data);
        fill();
    }

    public N2KTemperatureImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != ENVIRONMENT_TEMPERATURE_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", ENVIRONMENT_TEMPERATURE_PGN, header.getPgn()));
        fill();
    }

    private void fill() {

        sid = getByte(data, 0, 0xFF);
        instance = getByte(data, 1, 0xFF);

        source = parseEnum(data, 16, 0, 8, N2KLookupTables.getTable(TEMPERATURE_SOURCE));

        Double dT = parseDouble(data, 24, 16, 0.01, false);
        temperature = (dT == null) ? Double.NaN : Utils.round(dT - 273.15, 1);

        Double dST = parseDouble(data, 40, 16, 0.01, false);
        setTemperature = (dST == null) ? Double.NaN : Utils.round(dST - 273.15, 1);

    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public int getInstance() {
        return instance;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public double getTemperature() {
        return temperature;
    }

    @Override
    public double getSetTemperature() {
        return setTemperature;
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Instance {%d} TempSource {%s} Temperature {%.1f} SetTemperature {%.1f}",
                ENVIRONMENT_TEMPERATURE_PGN, getHeader().getSource(), getInstance(), getSource(), getTemperature(), getSetTemperature());
    }
}
