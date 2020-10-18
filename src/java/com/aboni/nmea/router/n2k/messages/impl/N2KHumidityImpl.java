package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.message.HumiditySource;
import com.aboni.nmea.router.message.MsgHumidity;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.ENVIRONMENT_HUMIDITY_PGN;

public class N2KHumidityImpl extends N2KMessageImpl implements MsgHumidity {

    private HumiditySource source;
    private int instance;
    private int sid;
    private double humidity;
    private double setHumidity;

    public N2KHumidityImpl(byte[] data) {
        super(getDefaultHeader(ENVIRONMENT_HUMIDITY_PGN), data);
        fill();
    }

    public N2KHumidityImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != ENVIRONMENT_HUMIDITY_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", ENVIRONMENT_HUMIDITY_PGN, header.getPgn()));
        fill();
    }

    private void fill() {

        sid = getByte(data, 0, 0xFF);
        instance = getByte(data, 1, 0xFF);

        source = HumiditySource.valueOf(getByte(data, 2, 0));

        Double dT = parseDouble(data, 24, 16, 0.004, false);
        humidity = (dT == null) ? Double.NaN : Utils.round(dT, 1);

        Double dST = parseDouble(data, 40, 16, 0.004, false);
        setHumidity = (dST == null) ? Double.NaN : Utils.round(dST, 1);

    }

    @Override
    public int getSID() {
        return sid;
    }

    public int getInstance() {
        return instance;
    }

    @Override
    public HumiditySource getHumiditySource() {
        return source;
    }

    @Override
    public double getHumidity() {
        return humidity;
    }

    public double getSetHumidity() {
        return setHumidity;
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Instance {%d} HumiditySource {%s} Humidity {%.1f} SetHumidity {%.1f}",
                ENVIRONMENT_HUMIDITY_PGN, getHeader().getSource(), getInstance(), getHumiditySource(), getHumidity(), getSetHumidity());
    }
}
