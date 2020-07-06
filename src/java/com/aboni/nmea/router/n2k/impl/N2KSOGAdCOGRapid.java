package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

public class N2KSOGAdCOGRapid extends N2KMessageImpl {

    private int sid;
    private double sog;
    private double cog;
    private String cogReference;

    public N2KSOGAdCOGRapid(byte[] data) {
        super(getDefaultHeader(getInternalPgn()), data);
        fill();
    }

    public N2KSOGAdCOGRapid(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != getInternalPgn())
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", getInternalPgn(), header.getPgn()));
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

        Double dCog = parseDouble(data, 16, 0, 16, 0.0001, false);
        cog = dCog == null ? Double.NaN : Utils.round(Math.toDegrees(dCog), 1);

        Double dSog = parseDouble(data, 32, 0, 16, 0.01, false);
        sog = dSog == null ? Double.NaN : Utils.round(dSog * 3600.0 / 1852.0, 2);

        cogReference = parseEnum(data, 8, 0, 2, N2KLookupTables.LOOKUP_DIRECTION_REFERENCE);
    }

    private static int getInternalPgn() {
        return 129026;
    }

    public int getSID() {
        return sid;
    }

    public double getSOG() {
        return sog;
    }

    public double getCOG() {
        return cog;
    }

    public String getCOGReference() {
        return cogReference;
    }

    public boolean isTrueCOG() {
        return "True".equals(cogReference);
    }
}
