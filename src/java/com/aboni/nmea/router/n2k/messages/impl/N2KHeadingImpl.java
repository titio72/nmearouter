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

import com.aboni.nmea.router.message.DirectionReference;
import com.aboni.nmea.router.message.MsgHeading;
import com.aboni.nmea.router.message.impl.MsgHeadingImpl;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.utils.Utils;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.HEADING_PGN;

public class N2KHeadingImpl extends N2KMessageImpl implements MsgHeading {

    private final MsgHeading msgHeading;

    public N2KHeadingImpl(byte[] data) {
        super(getDefaultHeader(HEADING_PGN), data);
        msgHeading = fill(data);
    }

    public N2KHeadingImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != HEADING_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", HEADING_PGN, header.getPgn()));
        msgHeading = fill(data);
    }

    private static MsgHeading fill(byte[] data) {
        int sid = N2KBitUtils.getByte(data, 0, 0xFF);

        Double dH = N2KBitUtils.parseDouble(data, 8, 16, 0.0001, false);
        double heading = dH == null ? Double.NaN : Utils.round(Math.toDegrees(dH), 1);

        Double dD = N2KBitUtils.parseDouble(data, 24, 16, 0.0001, true);
        double deviation = dD == null ? Double.NaN : Utils.round(Math.toDegrees(dD), 1);

        Double dV = N2KBitUtils.parseDouble(data, 40, 16, 0.0001, true);
        double variation = dV == null ? Double.NaN : Utils.round(Math.toDegrees(dV), 1);

        DirectionReference reference = DirectionReference.valueOf((int) N2KBitUtils.parseIntegerSafe(data, 56, 0, 2, 0));

        return new MsgHeadingImpl(sid, heading, variation, deviation, reference == DirectionReference.MAGNETIC);
    }

    @Override
    public int getSID() {
        return msgHeading.getSID();
    }

    @Override
    public double getHeading() {
        return msgHeading.getHeading();
    }

    @Override
    public double getDeviation() {
        return msgHeading.getDeviation();
    }

    @Override
    public double getVariation() {
        return msgHeading.getVariation();
    }

    @Override
    public DirectionReference getReference() {
        return msgHeading.getReference();
    }

    @Override
    public boolean isTrueHeading() {
        return msgHeading.isTrueHeading();
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Heading {%.1f} Ref {%s}",
                HEADING_PGN, getHeader().getSource(), getHeading(), getReference());
    }
}
