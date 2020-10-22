package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.message.HumiditySource;
import com.aboni.nmea.router.message.MsgHumidity;
import com.aboni.nmea.router.message.MsgHumidityImpl;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import org.json.JSONObject;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.ENVIRONMENT_HUMIDITY_PGN;

public class N2KHumidityImpl extends N2KMessageImpl implements MsgHumidity {

    private final MsgHumidity msgHumidity;

    public N2KHumidityImpl(byte[] data) {
        super(getDefaultHeader(ENVIRONMENT_HUMIDITY_PGN), data);
        msgHumidity = fill(data);
    }

    public N2KHumidityImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != ENVIRONMENT_HUMIDITY_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", ENVIRONMENT_HUMIDITY_PGN, header.getPgn()));
        msgHumidity = fill(data);
    }

    private static MsgHumidity fill(byte[] data) {

        int sid = BitUtils.getByte(data, 0, 0xFF);
        int instance = BitUtils.getByte(data, 1, 0xFF);

        HumiditySource source = HumiditySource.valueOf(BitUtils.getByte(data, 2, 0));

        Double dT = BitUtils.parseDouble(data, 24, 16, 0.004, true);
        double humidity = (dT == null) ? Double.NaN : Utils.round(dT, 1);

        Double dST = BitUtils.parseDouble(data, 40, 16, 0.004, true);
        double setHumidity = (dST == null) ? Double.NaN : Utils.round(dST, 1);

        return new MsgHumidityImpl(sid, instance, source, humidity, setHumidity);
    }

    @Override
    public int getSID() {
        return msgHumidity.getSID();
    }

    public int getInstance() {
        return msgHumidity.getInstance();
    }

    @Override
    public HumiditySource getHumiditySource() {
        return msgHumidity.getHumiditySource();
    }

    @Override
    public double getHumidity() {
        return msgHumidity.getHumidity();
    }

    public double getSetHumidity() {
        return msgHumidity.getSetHumidity();
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Instance {%d} HumiditySource {%s} Humidity {%.1f} SetHumidity {%.1f}",
                ENVIRONMENT_HUMIDITY_PGN, getHeader().getSource(), getInstance(), getHumiditySource(), getHumidity(), getSetHumidity());
    }

    @Override
    public JSONObject toJSON() {
        return msgHumidity.toJSON();
    }
}
