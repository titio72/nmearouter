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

package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.nmea.router.message.MsgSatellites;
import com.aboni.nmea.router.message.Satellite;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.SATELLITES_IN_VIEW_PGN;

public class N2KSatellitesImpl extends N2KMessageImpl implements MsgSatellites {

    private int sid;
    private final List<Satellite> satellites = new ArrayList<>();

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public int getNumberOfSats() {
        return satellites.size();
    }

    @Override
    public List<Satellite> getSatellites() {
        return Collections.unmodifiableList(satellites);
    }
/*
          "Order": 1, "Id": "sid", "BitLength": 8, "BitOffset": 0, "BitStart": 0, "Signed": false
          "Order": 2, "Id": "mode", "Name": "Mode", "BitLength": 2, "BitOffset": 8, "BitStart": 0, "Type": "Lookup table", "Signed": false, "EnumValues": [
              "name": "Range residuals used to calculate position",     "value": "3"
          "Order": 3, "Id": "reserved", "Name": "Reserved", "Description": "Reserved", "BitLength": 6, "BitOffset": 10, "BitStart": 2, "Type": "Binary data", "Signed": false
          "Order": 4, "Id": "satsInView", "Name": "Sats in View", "BitLength": 8, "BitOffset": 16, "BitStart": 0, "Signed": false

          "Order": 5, "Id": "prn", "Name": "PRN", "BitLength": 8, "BitOffset": 24, "BitStart": 0, "Signed": false
          "Order": 6, "Id": "elevation", "Name": "Elevation", "BitLength": 16, "BitOffset": 32, "BitStart": 0, "Units": "rad", "Resolution": "0.0001", "Signed": false
          "Order": 7, "Id": "azimuth", "Name": "Azimuth", "BitLength": 16, "BitOffset": 48, "BitStart": 0, "Units": "rad", "Resolution": "0.0001", "Signed": false
          "Order": 8, "Id": "snr", "Name": "SNR", "BitLength": 16, "BitOffset": 64, "BitStart": 0, "Units": "dB", "Resolution": "0.01", "Signed": false
          "Order": 9, "Id": "rangeResiduals", "Name": "Range residuals", "BitLength": 32, "BitOffset": 80, "BitStart": 0, "Signed": true
          "Order": 10, "Id": "status", "Name": "Status", "BitLength": 4, "BitOffset": 112, "BitStart": 0, "Type": "Lookup table", "Signed": false, "EnumValues": [
              "name": "Not tracked",     "value": "0"
              "name": "Tracked",     "value": "1"
              "name": "Used",     "value": "2"
              "name": "Not tracked+Diff",     "value": "3"
              "name": "Tracked+Diff",     "value": "4"
              "name": "Used+Diff",     "value": "5"
          "Order": 11, "Id": "reserved", "Name": "Reserved", "Description": "Reserved", "BitLength": 4, "BitOffset": 116, "BitStart": 4, "Type": "Binary data", "Signed": false
     */


    public N2KSatellitesImpl(byte[] data) {
        super(getDefaultHeader(SATELLITES_IN_VIEW_PGN), data);
        fill();
    }

    public N2KSatellitesImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != SATELLITES_IN_VIEW_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", SATELLITES_IN_VIEW_PGN, header.getPgn()));
        fill();
    }

    private static final int SIZE_OF_SAT = 96;

    private void fill() {
        sid = (int) parseIntegerSafe(data, 0, 0, 8, 0xFF);
        int nSat = (int) parseIntegerSafe(data, 16, 0, 8, 0);

        for (int i = 0; i < nSat; i++) {
            int offset = SIZE_OF_SAT * i + 24;
            Satellite s = new Satellite(
                    getByte(data, offset / 8, 0xFF),
                    (int) Math.toDegrees(parseDoubleSafe(data, 8 + offset, 16, 0.0001, false)),
                    (int) Math.toDegrees(parseDoubleSafe(data, 24 + offset, 16, 0.0001, false)),
                    (int) parseDoubleSafe(data, 40 + offset, 16, 0.01, false),
                    getByte(data, (88 + offset) / 8, 0xFF) & 0x0F);
            satellites.add(s);
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Satellite s : getSatellites()) {
            b.append(String.format("[Sat {%s} El {%d} Az {%d} Status {%s}] ",
                    s.getId(), s.getElevation(), s.getAzimuth(), s.getStatus()));
        }
        return String.format("PGN {%s} Source {%d} Sats {%s}", SATELLITES_IN_VIEW_PGN, getHeader().getSource(), b.toString());
    }
}
