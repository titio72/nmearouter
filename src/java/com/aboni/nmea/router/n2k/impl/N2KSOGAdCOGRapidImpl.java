package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.messages.N2KSOGAdCOGRapid;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.DIRECTION_REFERENCE;

public class N2KSOGAdCOGRapidImpl extends N2KMessageImpl implements N2KSOGAdCOGRapid {

    private int sid;
    private double sog;
    private double cog;
    private String cogReference;

    public N2KSOGAdCOGRapidImpl(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KSOGAdCOGRapidImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        /*
        "Order": 1"Id": "sid""Name": "SID""BitLength": 8"BitOffset": 0"BitStart": 0"Signed": false
        "Order": 2"Id": "cogReference""Name": "COG Reference""BitLength": 2"BitOffset": 8"BitStart": 0"Type": "Lookup table""Signed": false"EnumValues": [
            "name": "True""value": "0"
            "name": "Magnetic""value": "1"
            "name": "Error""value": "2"
            "name": "Null""value": "3"
        "Order": 4"Id": "cog""Name": "COG""BitLength": 16"BitOffset": 16"BitStart": 0"Units": "rad""Resolution": "0.0001""Signed": false
        "Order": 5"Id": "sog""Name": "SOG""BitLength": 16"BitOffset": 32"BitStart": 0"Units": "m/s""Resolution": "0.01""Signed": false
         */
        sid = getByte(data, 0, 0xFF);

        Double dCog = parseDouble(data, 16, 16, 0.0001, false);
        cog = dCog == null ? Double.NaN : Utils.round(Math.toDegrees(dCog), 1);

        Double dSog = parseDouble(data, 32, 16, 0.01, false);
        sog = dSog == null ? Double.NaN : Utils.round(dSog * 3600.0 / 1852.0, 2);

        cogReference = parseEnum(data, 8, 0, 2, N2KLookupTables.getTable(DIRECTION_REFERENCE));
    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public double getSOG() {
        return sog;
    }

    @Override
    public double getCOG() {
        return cog;
    }

    @Override
    public String getCOGReference() {
        return cogReference;
    }

    @Override
    public boolean isTrueCOG() {
        return "True".equals(cogReference);
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} SOG {%.1f} COG {%.1f} Ref {%s}", PGN, getHeader().getSource(), getSOG(), getCOG(), getCOGReference());
    }
}
