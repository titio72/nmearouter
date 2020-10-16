package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.message.MsgPressure;
import com.aboni.nmea.router.message.PressureSource;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.ENVIRONMENT_PRESSURE_PGN;

public class N2KPressureImpl extends N2KMessageImpl implements MsgPressure {

    private PressureSource source;
    private int instance;
    private int sid;
    private double pressure;

    public N2KPressureImpl(byte[] data) {
        super(getDefaultHeader(ENVIRONMENT_PRESSURE_PGN), data);
        fill();
    }

    public N2KPressureImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != ENVIRONMENT_PRESSURE_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", ENVIRONMENT_PRESSURE_PGN, header.getPgn()));
        fill();
    }

    private void fill() {

        sid = getByte(data, 0, 0xFF);
        instance = getByte(data, 1, 0xFF);

        source = PressureSource.valueOf(getByte(data, 2, 0));

        Double dT = parseDouble(data, 24, 32, 0.1, false);
        pressure = (dT == null) ? Double.NaN : Utils.round(dT * 10.0, 1);
    }

    @Override
    public int getSID() {
        return sid;
    }

    public int getInstance() {
        return instance;
    }

    @Override
    public PressureSource getPressureSource() {
        return source;
    }

    @Override
    public double getPressure() {
        return pressure;
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Instance {%d} PressureSource {%s} Pressure {%.1f}",
                ENVIRONMENT_PRESSURE_PGN, getHeader().getSource(), getInstance(), getPressureSource(), getPressure());
    }
}
