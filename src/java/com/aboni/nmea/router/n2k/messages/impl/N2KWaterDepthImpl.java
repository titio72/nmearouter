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

import com.aboni.nmea.router.message.MsgWaterDepth;
import com.aboni.nmea.router.message.impl.MsgWaterDepthImpl;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.DEPTH_PGN;

public class N2KWaterDepthImpl extends N2KMessageImpl implements MsgWaterDepth {

    private final MsgWaterDepth theMessage;

    public N2KWaterDepthImpl(byte[] data) {
        super(getDefaultHeader(DEPTH_PGN), data);
        theMessage = fill(data);
    }

    public N2KWaterDepthImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != DEPTH_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", DEPTH_PGN, header.getPgn()));
        theMessage = fill(data);
    }

    private static MsgWaterDepth fill(byte[] data) {
        int sid = BitUtils.getByte(data, 0, 0);
        double depth = BitUtils.parseDoubleSafe(data, 8, 32, 0.01, false);
        double offset = BitUtils.parseDoubleSafe(data, 40, 8, 0.001, false);
        double range = BitUtils.parseDoubleSafe(data, 56, 8, 10, false);
        return new MsgWaterDepthImpl(sid, depth, offset, range);
    }

    @Override
    public int getSID() {
        return theMessage.getSID();
    }

    @Override
    public double getDepth() {
        return theMessage.getDepth();
    }

    @Override
    public double getOffset() {
        return theMessage.getOffset();
    }

    @Override
    public double getRange() {
        return theMessage.getRange();
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Depth {%.1f} Offset {%.1f} Range {%.1f}",
                DEPTH_PGN, getHeader().getSource(), getDepth(), getOffset(), getRange());
    }
}
