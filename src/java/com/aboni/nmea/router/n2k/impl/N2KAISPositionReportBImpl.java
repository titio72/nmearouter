package com.aboni.nmea.router.n2k.impl;


import com.aboni.misc.Utils;
import com.aboni.nmea.router.AISPositionReport;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import net.sf.marineapi.nmea.util.Position;

import java.time.Instant;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.*;

public class N2KAISPositionReportBImpl extends N2KMessageImpl implements AISPositionReport {

    public static final int PGN = 129039;

    private int messageId;
    private String repeatIndicator;
    private String sMMSI;
    private Position position;
    private String positionAccuracy;
    private boolean sRAIM;
    private int timestamp;
    private double cog;
    private double sog;
    private double heading;
    private String aisTransceiverInfo;
    private String unitType;
    private boolean integratedDisplay;
    private boolean sDSC;
    private String band;
    private boolean canHandleMsg22;
    private String aisMode;
    private String aisCommunicationState;
    private long overrideTime = -1;

    public N2KAISPositionReportBImpl(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KAISPositionReportBImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
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
            position = new Position(lat, lon);
        }

        positionAccuracy = parseEnum(data, 104, 0, 1, N2KLookupTables.getTable(POSITION_ACCURACY));
        sRAIM = parseIntegerSafe(data, 105, 1, 1, 0) == 1;
        timestamp = (int) parseIntegerSafe(data, 106, 2, 6, 0xFF);

        cog = parseDoubleSafe(data, 112, 16, 0.0001, false);
        cog = Double.isNaN(cog) ? cog : Utils.round(Math.toDegrees(cog), 1);
        sog = parseDoubleSafe(data, 128, 16, 0.01, false);
        if (!Double.isNaN(sog)) sog = Utils.round(sog * 3600.0 / 1852.0, 1);
        heading = parseDoubleSafe(data, 168, 16, 0.0001, false);
        heading = Double.isNaN(heading) ? heading : Utils.round(Math.toDegrees(heading), 1);

        aisTransceiverInfo = parseEnum(data, 163, 3, 5, N2KLookupTables.getTable(AIS_TRANSCEIVER));

        int i = (int) parseIntegerSafe(data, 194, 2, 1, 0xFF);
        unitType = getUnitType(i);

        integratedDisplay = parseIntegerSafe(data, 195, 3, 1, 0) == 1;
        sDSC = parseIntegerSafe(data, 196, 4, 1, 0) == 1;
        canHandleMsg22 = parseIntegerSafe(data, 198, 6, 1, 0) == 1;

        band = parseIntegerSafe(data, 197, 5, 1, 0) == 0 ? "top 525 kHz of marine band" : "Entire marine band";
        aisMode = parseIntegerSafe(data, 199, 7, 1, 0) == 0 ? "Autonomous" : "Assigned";
        aisCommunicationState = parseIntegerSafe(data, 200, 0, 1, 0) == 0 ? "SOTDMA" : "ITDMA";
    }

    private String getUnitType(int i) {
        switch (i) {
            case 0:
                return "SOTDMA";
            case 1:
                return "CS";
            default:
                return null;
        }
    }

    public String getBand() {
        return band;
    }

    public String getAisMode() {
        return aisMode;
    }

    public boolean isIntegratedDisplay() {
        return integratedDisplay;
    }

    public boolean isCanHandleMsg22() {
        return canHandleMsg22;
    }

    public boolean isDSC() {
        return sDSC;
    }

    public String getUnitType() {
        return unitType;
    }

    public int getTimestamp() {
        return timestamp;
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
    public Position getPosition() {
        return position;
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
    public double getCog() {
        return cog;
    }

    @Override
    public double getSog() {
        return sog;
    }

    @Override
    public double getHeading() {
        return heading;
    }

    @Override
    public String getMMSI() {
        return sMMSI;
    }

    @Override
    public String getTimestampStatus() {
        switch (timestamp) {
            case 0xFF:
            case 0x60:
                return "Not available";
            case 61:
                return "Manual input mode";
            case 62:
                return "Dead reckoning mode";
            case 63:
                return "Positioning system is inoperative";
            default:
                return "Available";
        }
    }

    public String getAisTransceiverInfo() {
        return aisTransceiverInfo;
    }

    public String getAisCommunicationState() {
        return aisCommunicationState;
    }


    @Override
    public long getAge(long now) {
        if (getTimestamp()<=60) {
            Instant l = (getOverrrideTime()>0)?
                    Instant.ofEpochMilli(getOverrrideTime()):
                    getHeader().getTimestamp().plusNanos((long) (getTimestamp() * 1E06));
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
    public String toString() {
        return String.format("PGN {%s} Src {%d} MMSI {%s} AIS Class {%s} Lat {%s} Lon {%s} COG {%.1f} SOG {%.1f} Timestamp {%d}",
                PGN, getHeader().getSource(), getMMSI(), getAISClass(), Utils.formatLatitude(getPosition().getLatitude()), Utils.formatLongitude(getPosition().getLatitude()),
                getCog(), getSog(), getTimestamp()
        );
    }}