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

import com.aboni.nmea.router.message.DCType;
import com.aboni.nmea.router.message.MsgDCDetailedStatus;
import com.aboni.nmea.router.message.impl.MsgDCDetailedStatusImpl;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.DC_DETAILED_STATUS_PGN;

public class N2KDCDetailedStatusImpl extends N2KMessageImpl implements MsgDCDetailedStatus {

    private final MsgDCDetailedStatus msgDCStatus;

    public N2KDCDetailedStatusImpl(byte[] data) {
        super(getDefaultHeader(DC_DETAILED_STATUS_PGN), data);
        msgDCStatus = fill(data);
    }

    public N2KDCDetailedStatusImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != DC_DETAILED_STATUS_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", DC_DETAILED_STATUS_PGN, header.getPgn()));
        msgDCStatus = fill(data);
    }

    private static MsgDCDetailedStatus fill(byte[] data) {
        int sid = BitUtils.getByte(data, 0, 0xFF);
        int instance = BitUtils.getByte(data, 1, 0xFF);
        int type = BitUtils.getByte(data, 2, DCType.UNKNOWN.toValue());
        int soc = BitUtils.getByte(data, 3, 0xFF);
        int soh = BitUtils.getByte(data, 4, 0xFF);
        int ttg = BitUtils.get2ByteInt(data, 5);
        double ripple = BitUtils.parseDoubleSafe(data, 56, 16, 0.01, false);
        System.out.printf("SOC %d\n", soc);
        return new MsgDCDetailedStatusImpl(sid, instance, DCType.valueOf(type), soc / 100.0, soh / 100.0, ttg, ripple);
    }

    @Override
    public int getSID() {
        return msgDCStatus.getSID();
    }

    @Override
    public int getInstance() {
        return msgDCStatus.getInstance();
    }

    @Override
    public int getTimeToGo() {
        return msgDCStatus.getTimeToGo();
    }

    @Override
    public DCType getType() {
        return msgDCStatus.getType();
    }

    @Override
    public double getRippleVoltage() {
        return msgDCStatus.getRippleVoltage();
    }

    @Override
    public double getSOC() {
        return msgDCStatus.getSOC();
    }

    @Override
    public double getSOH() {
        return msgDCStatus.getSOH();
    }
}
