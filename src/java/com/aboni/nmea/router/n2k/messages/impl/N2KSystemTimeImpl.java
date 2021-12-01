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

import com.aboni.nmea.router.message.MsgSystemTime;
import com.aboni.nmea.router.message.impl.MsgSystemTimeImpl;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.ZoneId;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.SYSTEM_TIME;
import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.SYSTEM_TIME_PGN;

public class N2KSystemTimeImpl extends N2KMessageImpl implements MsgSystemTime {

    private final MsgSystemTime theTime;

    public N2KSystemTimeImpl(byte[] data) {
        super(getDefaultHeader(SYSTEM_TIME_PGN), data);
        theTime = fill(data);
    }

    public N2KSystemTimeImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != SYSTEM_TIME_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", SYSTEM_TIME_PGN, header.getPgn()));
        theTime = fill(data);
    }

    private MsgSystemTime fill(byte[] data) {

        int sid = BitUtils.getByte(data, 0, 0xFF);

        Long lDate = BitUtils.parseInteger(data, 16, 16);
        Double dTime = BitUtils.parseDouble(data, 32, 32, 0.0001, false);

        Instant time;
        if (lDate != null && dTime != null && !dTime.isNaN()) {
            Instant i = Instant.ofEpochMilli(0);
            time = i.atZone(ZoneId.of("UTC")).plusDays(lDate).plusNanos((long) (dTime * 1000000000L)).toInstant();
        } else {
            time = null;
        }

        String timeSourceType = BitUtils.parseEnum(data, 8, 0, 4, N2KLookupTables.getTable(SYSTEM_TIME));

        return new MsgSystemTimeImpl(sid, timeSourceType, time);
    }

    @Override
    public int getSID() {
        return theTime.getSID();
    }

    @Override
    public Instant getTime() {
        return theTime.getTime();
    }

    @Override
    public String getTimeSourceType() {
        return theTime.getTimeSourceType();
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Time {%s} Time Source {%s}",
                SYSTEM_TIME_PGN, getHeader().getSource(), getTime(), getTimeSourceType());
    }

    @Override
    public JSONObject toJSON() {
        return theTime.toJSON();
    }

}
