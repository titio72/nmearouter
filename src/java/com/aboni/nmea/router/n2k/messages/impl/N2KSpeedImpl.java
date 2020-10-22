package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.message.MsgSpeed;
import com.aboni.nmea.router.message.MsgSpeedImpl;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import org.json.JSONObject;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.WATER_REFERENCE;
import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.SPEED_PGN;

public class N2KSpeedImpl extends N2KMessageImpl implements MsgSpeed {

    private final MsgSpeed theSpeed;

    public N2KSpeedImpl(byte[] data) {
        super(getDefaultHeader(SPEED_PGN), data);
        theSpeed = fill(data);
    }

    public N2KSpeedImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != SPEED_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", SPEED_PGN, header.getPgn()));
        theSpeed = fill(data);
    }

    private static MsgSpeed fill(byte[] data) {

        int sid = BitUtils.getByte(data, 0, 0);

        Double dSpeedW = BitUtils.parseDouble(data, 8, 16, 0.01, false);
        double speedWaterRef = dSpeedW == null ? Double.NaN : Utils.round(dSpeedW * 3600.0 / 1852.0, 2);

        Double dSpeedG = BitUtils.parseDouble(data, 24, 16, 0.01, false);
        double speedGroundRef = dSpeedG == null ? Double.NaN : Utils.round(dSpeedG * 3600.0 / 1852.0, 2);

        String waterRefType = BitUtils.parseEnum(data, 40, 0, 8, N2KLookupTables.getTable(WATER_REFERENCE));

        int speedDirection = (int) BitUtils.parseIntegerSafe(data, 48, 0, 4, 1);

        return new MsgSpeedImpl(sid, speedWaterRef, speedGroundRef, waterRefType, speedDirection);
    }

    @Override
    public int getSID() {
        return theSpeed.getSID();
    }

    @Override
    public double getSpeedWaterRef() {
        return theSpeed.getSpeedWaterRef();
    }

    @Override
    public double getSpeedGroundRef() {
        return theSpeed.getSpeedGroundRef();
    }

    @Override
    public String getSpeedSensorType() {
        return theSpeed.getSpeedSensorType();
    }

    @Override
    public int getSpeedDirection() {
        return theSpeed.getSpeedDirection();
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Water Speed {%.1f}",
                SPEED_PGN, getHeader().getSource(), getSpeedWaterRef());
    }

    @Override
    public JSONObject toJSON() {
        return theSpeed.toJSON();
    }
}
