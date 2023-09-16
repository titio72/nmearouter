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

import com.aboni.nmea.router.AISStaticData;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.*;
import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.AIS_STATIC_DATA_CLASS_B_PART_B_PGN;

public class N2KAISStaticDataBPartBImpl extends N2KMessageImpl implements AISStaticData {

    private int messageId;
    private String sMMSI;
    private int repeatIndicator;
    private String typeOfShip;
    private String callSign;
    private double length;
    private double beam;
    private String aisTransceiverInformation;

    public N2KAISStaticDataBPartBImpl(byte[] data) {
        super(getDefaultHeader(AIS_STATIC_DATA_CLASS_B_PART_B_PGN), data);
        fill();
    }

    public N2KAISStaticDataBPartBImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != AIS_STATIC_DATA_CLASS_B_PART_B_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", AIS_STATIC_DATA_CLASS_B_PART_B_PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        messageId = (int) N2KBitUtils.parseIntegerSafe(data, 0, 0, 6, 0xFF);
        repeatIndicator = (int)N2KBitUtils.parseIntegerSafe(data, 6, 6, 2, 0);
        sMMSI = String.format("%d", N2KBitUtils.parseIntegerSafe(data, 8, 0, 32, 0));
        typeOfShip = N2KBitUtils.parseEnum(data, 40, 0, 8, N2KLookupTables.getTable(SHIP_TYPE));
        callSign = N2KBitUtils.getText(data, 13, 7);
        length = N2KBitUtils.parseDoubleSafe(data, 160, 16, 0.1, false);
        beam = N2KBitUtils.parseDoubleSafe(data, 176, 16, 0.1, false);
        aisTransceiverInformation = N2KBitUtils.parseEnum(data, 264, 0, 5, N2KLookupTables.getTable(AIS_TRANSCEIVER));
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
    public int getRepeatIndicator() {
        return repeatIndicator;
    }

    @Override
    public String getName() {
        return null;
    }

    public String getCallSign() {
        return callSign;
    }

    public double getLength() {
        return length;
    }

    public double getBeam() {
        return beam;
    }

    @Override
    public String getAISClass() {
        return "B";
    }

    @Override
    public String getTypeOfShip() {
        return typeOfShip;
    }

    @Override
    public String getAisTransceiverInfo() {
        return aisTransceiverInformation;
    }


    @Override
    public String toString() {
        return String.format("PGN {%s} Src {%d} MsgId {%d} MMSI {%s} Repeat {%s} Type {%s} Callsign {%s} " +
                        "AISClass {%s} Length {%.1f} Beam {%.1f} Transceiver {%s}",
                AIS_STATIC_DATA_CLASS_B_PART_B_PGN, getHeader().getSource(),
                getMessageId(), getMMSI(), getRepeatIndicator(), getTypeOfShip(), getCallSign(),
                getAISClass(), getLength(), getBeam(), getAisTransceiverInfo());
    }

    @Override
    public String getMessageContentType() {
        return "AISStaticDataBPartB";
    }
}