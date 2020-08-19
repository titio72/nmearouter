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

import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.messages.N2KSystemTime;

import java.time.Instant;
import java.time.ZoneId;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.SYSTEM_TIME;

public class N2KSystemTimeImpl extends N2KMessageImpl implements N2KSystemTime {

    private int sid;
    private Instant time;
    private String timeSourceType;

    public N2KSystemTimeImpl(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KSystemTimeImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    private void fill() {

        sid = getByte(data, 0, 0xFF);

        Long lDate = parseInteger(data, 16, 16);
        Double dTime = parseDouble(data, 32, 32, 0.0001, false);

        if (lDate != null && dTime != null && !dTime.isNaN()) {
            Instant i = Instant.ofEpochMilli(0);
            time = i.atZone(ZoneId.of("UTC")).plusDays(lDate).plusNanos((long) (dTime * 1000000000L)).toInstant();
        } else {
            time = null;
        }

        timeSourceType = parseEnum(data, 8, 0, 4, N2KLookupTables.getTable(SYSTEM_TIME));
    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public Instant getTime() {
        return time;
    }

    @Override
    public String getTimeSourceType() {
        return timeSourceType;
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d}Time {%s} Time Source {%s}", PGN, getHeader().getSource(), getTime(), getTimeSourceType());
    }
}
