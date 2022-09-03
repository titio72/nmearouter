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
import com.aboni.nmea.router.AISStaticData;
import com.aboni.nmea.router.message.GNSSInfo;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import net.sf.marineapi.nmea.util.Position;

import java.time.Instant;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.*;
import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.AIS_ATON_PGN;

public class N2KAISAtoN extends N2KMessageImpl implements AISPositionReport, AISStaticData {

    private int messageId;
    private String sMMSI;
    private String repeatIndicator;
    private String positionAccuracy;
    private boolean sRAIM;
    private int timestamp;
    private String name;
    private String sAtoNType;
    private String aisTransceiverInfo;
    private int aisSpare;
    private String positionFixingDeviceType;
    private long overrideTime = -1;

    private final GNSSInfoImpl gpsInfo = new GNSSInfoImpl();

    public N2KAISAtoN(byte[] data) {
        super(getDefaultHeader(AIS_ATON_PGN), data);
        fill();
    }

    public N2KAISAtoN(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != AIS_ATON_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", AIS_ATON_PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        messageId = (int) BitUtils.parseIntegerSafe(data, 0, 0, 6, 0xFF);
        repeatIndicator = BitUtils.parseEnum(data, 6, 6, 2, N2KLookupTables.getTable(REPEAT_INDICATOR));
        sMMSI = String.format("%d", BitUtils.parseIntegerSafe(data, 8, 0, 32, 0));

        double lon = BitUtils.parseDoubleSafe(data, 40, 32, 0.0000001, true);
        double lat = BitUtils.parseDoubleSafe(data, 72, 32, 0.0000001, true);
        if (!(Double.isNaN(lon) || Double.isNaN(lat))) {
            gpsInfo.setPosition(new Position(lat, lon));
        }
        gpsInfo.setCOG(0.0);
        gpsInfo.setSOG(0.0);

        positionAccuracy = BitUtils.parseEnum(data, 104, 0, 1, N2KLookupTables.getTable(POSITION_ACCURACY));
        sRAIM = BitUtils.parseIntegerSafe(data, 105, 1, 1, 0) == 1;
        timestamp = (int) BitUtils.parseIntegerSafe(data, 106, 2, 6, 0xFF);

        sAtoNType = BitUtils.parseEnum(data, 176, 0, 5, N2KLookupTables.getTable(ATON_TYPE));
        name = BitUtils.getText(data, 26, 34);

        aisTransceiverInfo = BitUtils.parseEnum(data, 200, 0, 5, N2KLookupTables.getTable(AIS_TRANSCEIVER));
        aisSpare = (int) BitUtils.parseIntegerSafe(data, 184, 0, 1, 0xFF);
        positionFixingDeviceType = BitUtils.parseEnum(data, 185, 1, 4, N2KLookupTables.getTable(POSITION_FIX_DEVICE));
    }

    @Override
    public String getAISClass() {
        return "X";
    }

    @Override
    public double getLength() {
        return 0;
    }

    @Override
    public double getBeam() {
        return 0;
    }

    @Override
    public String getTypeOfShip() {
        return getAtoNType();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCallSign() {
        return "";
    }

    @Override
    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public String getNavStatus() {
        return "";
    }

    @Override
    public long getAge(long now) {
        if (getTimestamp() <= 60) {
            Instant l = (getOverrideTime() > 0) ?
                    Instant.ofEpochMilli(getOverrideTime()) :
                    getHeader().getTimestamp().plusNanos((long) (getTimestamp() * 1E06));
            return now - l.toEpochMilli();
        } else {
            return -1;
        }
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

    @Override
    public int getMessageId() {
        return messageId;
    }

    @Override
    public String getMMSI() {
        return sMMSI;
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
        return 0;
    }

    @Override
    public String getPositionAccuracy() {
        return positionAccuracy;
    }

    public int getAisSpare() {
        return aisSpare;
    }

    @Override
    public String getAisTransceiverInfo() {
        return aisTransceiverInfo;
    }

    public String getAtoNType() {
        return sAtoNType;
    }

    public String getPositionFixingDeviceType() {
        return positionFixingDeviceType;
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
        return gpsInfo;
    }

    @Override
    public String toString() {
        return String.format("PGN {%d} Source {%d} MMSI {%s} Position {%s} Name {%s} Type {%s}",
                getHeader().getPgn(), getHeader().getSource(), getMMSI(), getGPSInfo().getPosition(), getName(), getAtoNType());
    }

    @Override
    public String getMessageContentType() {
        return "AISAtoN";
    }
}
