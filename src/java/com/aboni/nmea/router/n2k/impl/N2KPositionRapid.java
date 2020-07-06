package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import net.sf.marineapi.nmea.util.Position;

public class N2KPositionRapid extends N2KMessageImpl {

    private double latitude;
    private double longitude;


    public N2KPositionRapid(byte[] data) throws PGNDataParseException {
        super(getDefaultHeader(getInternalPgn()), data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != getInternalPgn())
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", getInternalPgn(), header.getPgn()));
        fill();
    }

    protected N2KPositionRapid(N2KMessageHeader header, byte[] data) {
        super(header, data);
        fill();
    }

    private void fill() {
        /*
        "Order": 1, "Id": "latitude","BitLength": 32,"BitOffset": 0,"BitStart": 0,"Units": "deg","Type": "Latitude","Resolution": "0.0000001","Signed": true
        "Order": 2,"Id": "longitude","BitLength": 32,"BitOffset": 32,"BitStart": 0,"Units": "deg","Type": "Longitude","Resolution": "0.0000001","Signed": true
         */
        Double dLat = parseDouble(data, 0, 0, 32, 0.0000001, true);
        latitude = dLat == null ? Double.NaN : dLat;

        Double dLon = parseDouble(data, 32, 0, 32, 0.0000001, true);
        longitude = dLon == null ? Double.NaN : dLon;
    }

    private static int getInternalPgn() {
        return 129025;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Position getPosition() {
        if (!Double.isNaN(latitude) && !Double.isNaN(longitude))
            return new Position(latitude, longitude);
        else
            return null;
    }
}
