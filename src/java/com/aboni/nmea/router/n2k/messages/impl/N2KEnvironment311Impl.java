package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.message.MsgEnvironment;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.HUMIDITY_SOURCE;
import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.TEMPERATURE_SOURCE;
import static com.aboni.nmea.router.n2k.messages.N2kMessagePGNs.ENVIRONMENT_130311_PGN;

public class N2KEnvironment311Impl extends N2KMessageImpl implements MsgEnvironment {

    private String humiditySource;
    private String tempSource;
    private int sid;
    private double temperature;
    private double humidity;
    private double atmosphericPressure;


    public N2KEnvironment311Impl(byte[] data) {
        super(getDefaultHeader(ENVIRONMENT_130311_PGN), data);
        fill();
    }

    public N2KEnvironment311Impl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != ENVIRONMENT_130311_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", ENVIRONMENT_130311_PGN, header.getPgn()));
        fill();
    }

    private void fill() {

        sid = getByte(data, 0, 0xFF);

        tempSource = parseEnum(data, 8, 0, 6, N2KLookupTables.getTable(TEMPERATURE_SOURCE));

        humiditySource = parseEnum(data, 14, 6, 2, N2KLookupTables.getTable(HUMIDITY_SOURCE));

        Double dT = parseDouble(data, 16, 16, 0.01, false);
        temperature = (dT == null) ? Double.NaN : Utils.round(dT - 273.15, 1);

        Double dH = parseDouble(data, 32, 16, 0.004, true);
        humidity = (dH == null) ? Double.NaN : dH;

        Long dP = parseInteger(data, 48, 16);
        atmosphericPressure = (dP == null) ? Double.NaN : dP;

    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public String getHumiditySource() {
        return humiditySource;
    }

    @Override
    public String getTempSource() {
        return tempSource;
    }

    @Override
    public double getTemperature() {
        return temperature;
    }

    @Override
    public double getHumidity() {
        return humidity;
    }

    @Override
    public double getAtmosphericPressure() {
        return atmosphericPressure;
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Humidity {%.1f} Hum. Source {%s} Temperature {%.1f} Temp. Source {%s} Atmo. Pressure {%.1f}",
                ENVIRONMENT_130311_PGN, getHeader().getSource(), getHumidity(), getHumiditySource(), getTemperature(), getTempSource(), getAtmosphericPressure());
    }
}
