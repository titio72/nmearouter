package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.AISPositionReport;
import com.aboni.nmea.router.GNSSInfo;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import net.sf.marineapi.nmea.util.Position;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.*;

public class N2KAISUTCPositionReportImpl extends N2KMessageImpl implements AISPositionReport {

    public static final int PGN = 129793;

    private int messageId;
    private String repeatIndicator;
    private String sMMSI;
    private String positionAccuracy;
    private boolean sRAIM;
    private String aisTransceiverInfo;
    private String aisCommunicationState;
    private final GNSSInfoImpl gpsInfo = new GNSSInfoImpl();
    private long overrideTime = -1;
    private Instant utc;
    private String gnssType;

    public N2KAISUTCPositionReportImpl(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KAISUTCPositionReportImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    protected void fill() {
        messageId = (int) parseIntegerSafe(data, 0, 0, 6, 0xFF);
        repeatIndicator = parseEnum(data, 6, 6, 2, N2KLookupTables.getTable(REPEAT_INDICATOR));
        sMMSI = String.format("%d", parseIntegerSafe(data, 8, 0, 32, 0));

        double lon = parseDoubleSafe(data, 40, 32, 0.0000001, true);
        double lat = parseDoubleSafe(data, 72, 32, 0.0000001, true);
        if (!(Double.isNaN(lon) || Double.isNaN(lat))) {
            gpsInfo.setPosition(new Position(lat, lon));
        }

        positionAccuracy = parseEnum(data, 104, 0, 1, N2KLookupTables.getTable(POSITION_ACCURACY));
        sRAIM = parseIntegerSafe(data, 105, 1, 1, 0) == 1;

        aisCommunicationState = parseIntegerSafe(data, 144, 0, 19, 0) == 0 ? "SOTDMA" : "ITDMA";
        aisTransceiverInfo = parseEnum(data, 163, 3, 5, N2KLookupTables.getTable(AIS_TRANSCEIVER));

        double secsToMidnight = parseDoubleSafe(data, 112, 32, 0.0001, false);
        long daysSince1970 = parseIntegerSafe(data, 168, 0, 16, -1);
        if (!Double.isNaN(secsToMidnight) && daysSince1970 > 0) {
            LocalDateTime s = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
            s = s.plusDays(daysSince1970);
            s = s.plusSeconds((long) secsToMidnight);
            utc = s.toInstant(ZoneOffset.UTC);
        }

        gnssType = parseEnum(data, 188, 4, 4, N2KLookupTables.getTable(POSITION_FIX_DEVICE));
    }

    @Override
    public String getNavStatus() {
        return null;
    }

    @Override
    public String getAISClass() {
        return "B";
    }

    @Override
    public int getMessageId() {
        return messageId;
    }

    @Override
    public String getPositionAccuracy() {
        return positionAccuracy;
    }

    @Override
    public String getRepeatIndicator() {
        return repeatIndicator;
    }

    @Override
    public boolean issRAIM() {
        return sRAIM;
    }

    @Override
    public double getHeading() {
        return Double.NaN;
    }

    @Override
    public String getMMSI() {
        return sMMSI;
    }

    @Override
    public String getTimestampStatus() {
        return "Available";
    }

    @Override
    public int getTimestamp() {
        return 0;
    }

    public String getAisTransceiverInfo() {
        return aisTransceiverInfo;
    }

    public String getAisCommunicationState() {
        return aisCommunicationState;
    }

    public Instant getUtc() {
        return utc;
    }

    @Override
    public long getAge(long now) {
        if (utc != null) {
            Instant l = (getOverrrideTime() > 0) ?
                    Instant.ofEpochMilli(getOverrrideTime()) : utc;
            return now - l.toEpochMilli();
        } else {
            return -1;
        }
    }

    @Override
    public void setOverrideTime(long t) {
        overrideTime = t;
    }

    @Override
    public long getOverrrideTime() {
        return overrideTime;
    }

    @Override
    public GNSSInfo getGPSInfo() {
        return gpsInfo;
    }

    public String getGNSSType() {
        return gnssType;
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Src {%d} MMSI {%s} AIS Class {%s} Lat {%s} Lon {%s} COG {%.1f} SOG {%.1f} Time {%s}",
                PGN, getHeader().getSource(), getMMSI(), getAISClass(),
                Utils.formatLatitude(gpsInfo.getPosition().getLatitude()),
                Utils.formatLongitude(gpsInfo.getPosition().getLatitude()),
                gpsInfo.getCOG(), gpsInfo.getSOG(), utc
        );
    }
}
