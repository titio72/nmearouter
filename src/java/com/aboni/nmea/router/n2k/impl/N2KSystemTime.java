package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import java.time.Instant;
import java.time.ZoneId;

public class N2KSystemTime extends N2KMessageImpl {

    private int sid;
    private Instant time;
    private String timeSourceType;

    public N2KSystemTime(byte[] data) {
        super(getDefaultHeader(getInternalPgn()), data);
        fill();
    }

    protected N2KSystemTime(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != getInternalPgn())
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", getInternalPgn(), header.getPgn()));
        fill();
    }

    private void fill() {

        sid = getByte(data, 0, 0xFF);

        Long lDate = parseInteger(data, 16, 0, 16, false);
        Double dTime = parseDouble(data, 32, 0, 32, 0.0001, false);

        if (lDate != null && dTime != null && !dTime.isNaN()) {
            Instant i = Instant.ofEpochMilli(0);
            time = i.atZone(ZoneId.of("UTC")).plusDays(lDate).plusNanos((long) (dTime * 1000000000L)).toInstant();
        } else {
            time = null;
        }

        timeSourceType = parseEnum(data, 8, 0, 4, N2KLookupTables.LOOKUP_SYSTEM_TIME);
    }

    private static int getInternalPgn() {
        return 126992;
    }

    public int getSID() {
        return sid;
    }

    public Instant getTime() {
        return time;
    }

    public String getTimeSourceType() {
        return timeSourceType;
    }
}
