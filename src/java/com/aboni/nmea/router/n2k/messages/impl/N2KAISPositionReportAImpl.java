/*
 * Copyright (c) 2020,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.nmea.router.AISPositionReport;
import com.aboni.nmea.router.message.GNSSInfo;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.utils.Utils;
import net.sf.marineapi.nmea.util.Position;

import java.time.Instant;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.*;
import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.AIS_POSITION_REPORT_CLASS_A_PGN;

public class N2KAISPositionReportAImpl extends N2KMessageImpl implements AISPositionReport {

    private int messageId;
    private String repeatIndicator;
    private String sMMSI;
    private String positionAccuracy;
    private boolean sRAIM;
    private int timestamp;
    private double heading;
    private double rateOfTurn;
    private String aisTransceiverInfo;
    private String navStatus;
    private String specialManeuverIndicator;
    private int aisSpare;
    private int seqId;
    private final GNSSInfoImpl gnssInfo = new GNSSInfoImpl();

    private long overrideTime = -1;

    public N2KAISPositionReportAImpl(byte[] data) {
        super(getDefaultHeader(AIS_POSITION_REPORT_CLASS_A_PGN), data);
        fill();
    }

    public N2KAISPositionReportAImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != AIS_POSITION_REPORT_CLASS_A_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", AIS_POSITION_REPORT_CLASS_A_PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        messageId = (int) BitUtils.parseIntegerSafe(data, 0, 0, 6, 0xFF);
        repeatIndicator = BitUtils.parseEnum(data, 6, 6, 2, N2KLookupTables.getTable(REPEAT_INDICATOR));
        sMMSI = String.format("%d", BitUtils.parseIntegerSafe(data, 8, 0, 32, 0));

        double lon = BitUtils.parseDoubleSafe(data, 40, 32, 0.0000001, true);
        double lat = BitUtils.parseDoubleSafe(data, 72, 32, 0.0000001, true);
        if (!(Double.isNaN(lon) || Double.isNaN(lat))) {
            gnssInfo.setPosition(new Position(lat, lon));
        }

        positionAccuracy = BitUtils.parseEnum(data, 104, 0, 1, N2KLookupTables.getTable(POSITION_ACCURACY));
        sRAIM = BitUtils.parseIntegerSafe(data, 105, 1, 1, 0) == 1;
        timestamp = (int) BitUtils.parseIntegerSafe(data, 106, 2, 6, 0xFF);

        gnssInfo.setCOG(BitUtils.parseDoubleSafe(data, 112, 16, 0.0001, false));
        gnssInfo.setCOG(Double.isNaN(gnssInfo.getCOG()) ? gnssInfo.getCOG() : Utils.round(Math.toDegrees(gnssInfo.getCOG()), 1));
        gnssInfo.setSOG(BitUtils.parseDoubleSafe(data, 128, 16, 0.01, false));
        if (!Double.isNaN(gnssInfo.getSOG())) gnssInfo.setSOG(Utils.round(gnssInfo.getSOG() * 3600.0 / 1852.0, 1));

        heading = BitUtils.parseDoubleSafe(data, 168, 16, 0.0001, false);
        heading = Double.isNaN(heading) ? heading : Utils.round(Math.toDegrees(heading), 1);
        rateOfTurn = BitUtils.parseDoubleSafe(data, 184, 16, 0.0001, false);
        rateOfTurn = Double.isNaN(rateOfTurn) ? heading : Utils.round(Math.toDegrees(rateOfTurn), 1);

        aisSpare = (int) BitUtils.parseIntegerSafe(data, 208, 0, 3, 0xFF);
        seqId = (int) BitUtils.parseIntegerSafe(data, 216, 0, 8, 0xFF);

        aisTransceiverInfo = BitUtils.parseEnum(data, 163, 3, 5, N2KLookupTables.getTable(AIS_TRANSCEIVER));

        navStatus = BitUtils.parseEnum(data, 200, 0, 4, N2KLookupTables.getTable(NAV_STATUS));

        specialManeuverIndicator = BitUtils.parseEnum(data, 204, 4, 2, N2KLookupTables.getTable(AIS_SPECIAL_MANEUVER));
    }

    @Override
    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public String getAISClass() {
        return "A";
    }

    public Position getPosition() {
        return gnssInfo.getPosition();
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

    public double getCOG() {
        return gnssInfo.getCOG();
    }

    public double getSOG() {
        return gnssInfo.getSOG();
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
            Instant l = (getOverrideTime()>0)?
                    Instant.ofEpochMilli(getOverrideTime()):
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
    public long getOverrideTime() {
        return overrideTime;
    }

    @Override
    public GNSSInfo getGPSInfo() {
        return gnssInfo;
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Src {%d} MMSI {%s} AIS Class {%s} Position {%s} COG {%.1f} SOG {%.1f} Timestamp {%d}",
                AIS_POSITION_REPORT_CLASS_A_PGN, getHeader().getSource(), getMMSI(), getAISClass(),
                getPosition(), getCOG(), getSOG(), getTimestamp()
        );
    }

    @Override
    public String getMessageContentType() {
        return "AISPositionReportA";
    }
}