package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.message.MsgTemperature;
import com.aboni.nmea.router.message.TemperatureSource;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.ENVIRONMENT_TEMPERATURE_PGN;

public class N2KTemperatureImpl extends N2KMessageImpl implements MsgTemperature {

    private TemperatureSource source;
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

        source = TemperatureSource.valueOf(getByte(data, 2, 0));

        Double dT = parseDouble(data, 24, 16, 0.01, false);
        temperature = (dT == null) ? Double.NaN : Utils.round(
                dT>200.0?(dT - 273.15):dT, 1);

        Double dST = parseDouble(data, 40, 16, 0.01, false);
        setTemperature = (dST == null) ? Double.NaN : Utils.round(
                dST>200.0?(dST - 273.15):dST, 1);

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
    public TemperatureSource getTemperatureSource() {
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
                ENVIRONMENT_TEMPERATURE_PGN, getHeader().getSource(), getInstance(), getTemperatureSource(), getTemperature(), getSetTemperature());
    }
}
