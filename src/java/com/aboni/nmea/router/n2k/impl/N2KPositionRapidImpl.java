/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.messages.N2KPositionRapid;
import net.sf.marineapi.nmea.util.Position;

public class N2KPositionRapidImpl extends N2KMessageImpl implements N2KPositionRapid {

    private double latitude;
    private double longitude;

    public N2KPositionRapidImpl(byte[] data) throws PGNDataParseException {
        super(getDefaultHeader(PGN), data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    public N2KPositionRapidImpl(N2KMessageHeader header, byte[] data) {
        super(header, data);
        fill();
    }

    private void fill() {
        /*
        "Order": 1, "Id": "latitude","BitLength": 32,"BitOffset": 0,"BitStart": 0,"Units": "deg","Type": "Latitude","Resolution": "0.0000001","Signed": true
        "Order": 2,"Id": "longitude","BitLength": 32,"BitOffset": 32,"BitStart": 0,"Units": "deg","Type": "Longitude","Resolution": "0.0000001","Signed": true
         */
        Double dLat = parseDouble(data, 0, 32, 0.0000001, true);
        latitude = dLat == null ? Double.NaN : dLat;

        Double dLon = parseDouble(data, 32, 32, 0.0000001, true);
        longitude = dLon == null ? Double.NaN : dLon;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public Position getPosition() {
        if (isValidDouble(latitude) && isValidDouble(longitude))
            return new Position(latitude, longitude);
        else
            return null;
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Lat {%s} Lon {%s}",
                PGN, getHeader().getSource(),
                (getPosition() == null) ? "" : Utils.formatLatitude(getPosition().getLatitude()),
                (getPosition() == null) ? "" : Utils.formatLongitude(getPosition().getLongitude()));
    }
}
