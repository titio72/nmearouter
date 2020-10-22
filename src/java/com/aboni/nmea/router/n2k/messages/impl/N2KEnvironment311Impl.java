package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.HUMIDITY_SOURCE;
import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.TEMPERATURE_SOURCE;
import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.ENVIRONMENT_130311_PGN;

public class N2KEnvironment311Impl extends N2KMessageImpl {

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

        sid = BitUtils.getByte(data, 0, 0xFF);

        tempSource = BitUtils.parseEnum(data, 8, 0, 6, N2KLookupTables.getTable(TEMPERATURE_SOURCE));

        humiditySource = BitUtils.parseEnum(data, 14, 6, 2, N2KLookupTables.getTable(HUMIDITY_SOURCE));

        Double dT = BitUtils.parseDouble(data, 16, 16, 0.01, false);
        temperature = (dT == null) ? Double.NaN : Utils.round(dT - 273.15, 1);

        Double dH = BitUtils.parseDouble(data, 32, 16, 0.004, true);
        humidity = (dH == null) ? Double.NaN : dH;

        Long dP = BitUtils.parseInteger(data, 48, 16);
        atmosphericPressure = (dP == null) ? Double.NaN : dP;

    }

    public int getSID() {
        return sid;
    }

    public String getHumiditySource() {
        return humiditySource;
    }

    public String getTemperatureSource() {
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

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Humidity {%.1f} Hum. Source {%s} Temperature {%.1f} Temp. Source {%s} Atmo. Pressure {%.1f}",
                ENVIRONMENT_130311_PGN, getHeader().getSource(), getHumidity(), getHumiditySource(), getTemperature(), getTemperatureSource(), getAtmosphericPressure());
    }
}
