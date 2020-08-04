package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.AISPositionReport;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import net.sf.marineapi.nmea.util.Position;

import java.time.Instant;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.*;

public class N2KAISPositionReportA extends N2KMessageImpl implements AISPositionReport {

    public static final int PGN = 129038;

    public N2KAISPositionReportA(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KAISPositionReportA(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        messageId = (int) parseIntegerSafe(data, 0, 0, 6, false, 0xFF);
        repeatIndicator = parseEnum(data, 6, 6, 2, N2KLookupTables.getTable(REPEAT_INDICATOR));
        sMMSI = String.format("%d", parseIntegerSafe(data, 8, 0, 32, false, 0));

        double lon = parseDoubleSafe(data, 40, 0, 32, 0.0000001, true);
        double lat = parseDoubleSafe(data, 72, 0, 32, 0.0000001, true);
        if (!(Double.isNaN(lon) || Double.isNaN(lat))) {
            position = new Position(lat, lon);
        }

        positionAccuracy = parseEnum(data, 104, 0, 1, N2KLookupTables.getTable(POSITION_ACCURACY));
        sRAIM = parseIntegerSafe(data, 105, 1, 1, false, 0) == 1;
        timestamp = (int) parseIntegerSafe(data, 106, 2, 6, false, 0xFF);

        cog = parseDoubleSafe(data, 112, 0, 16, 0.0001, false);
        cog = Double.isNaN(cog) ? cog : Utils.round(Math.toDegrees(cog), 1);
        sog = parseDoubleSafe(data, 128, 0, 16, 0.01, false);
        if (!Double.isNaN(sog)) sog = Utils.round(sog * 3600.0 / 1852.0, 1);

        heading = parseDoubleSafe(data, 168, 0, 16, 0.0001, false);
        heading = Double.isNaN(heading) ? heading : Utils.round(Math.toDegrees(heading), 1);
        rateOfTurn = parseDoubleSafe(data, 184, 0, 16, 0.0001, false);
        rateOfTurn = Double.isNaN(rateOfTurn) ? heading : Utils.round(Math.toDegrees(rateOfTurn), 1);

        aisSpare = (int) parseIntegerSafe(data, 208, 0, 3, false, 0xFF);
        seqId = (int) parseIntegerSafe(data, 216, 0, 8, false, 0xFF);

        aisTransceiverInfo = parseEnum(data, 163, 3, 5, N2KLookupTables.getTable(AIS_TRANSCEIVER));

        navStatus = parseEnum(data, 200, 0, 4, N2KLookupTables.getTable(NAV_STATUS));

        specialManeuverIndicator = parseEnum(data, 204, 4, 2, N2KLookupTables.getTable(AIS_SPECIAL_MANEUVER));
    }

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
    private double rateOfTurn;
    private String aisTransceiverInfo;
    private String navStatus;
    private String specialManeuverIndicator;
    private int aisSpare;
    private int seqId;

    private long overrideTime = -1;

    @Override
    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public String getAISClass() {
        return "A";
    }

    public Position getPosition() {
        return position;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getPositionAccuracy() {
        return positionAccuracy;
    }

    public String getRepeatIndicator() {
        return repeatIndicator;
    }

    public boolean issRAIM() {
        return sRAIM;
    }

    public double getCog() {
        return cog;
    }

    public double getSog() {
        return sog;
    }

    public double getHeading() {
        return heading;
    }

    public double getRateOfTurn() {
        return rateOfTurn;
    }

    public String getMMSI() {
        return sMMSI;
    }

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

    public int getAisSpare() {
        return aisSpare;
    }

    public int getSeqId() {
        return seqId;
    }

    @Override
    public String getNavStatus() {
        return navStatus;
    }

    public String getSpecialManeuverIndicator() {
        return specialManeuverIndicator;
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
}