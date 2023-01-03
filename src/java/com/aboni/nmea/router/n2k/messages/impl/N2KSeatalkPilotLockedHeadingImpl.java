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

import com.aboni.nmea.router.message.MsgSeatalkPilotLockedHeading;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.utils.Utils;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.SEATALK_PILOT_LOCKED_HEADING_PGN;

public class N2KSeatalkPilotLockedHeadingImpl extends N2KMessageImpl implements MsgSeatalkPilotLockedHeading {

    private double lockedHeadingTrue;
    private double lockedHeadingMagnetic;

    public N2KSeatalkPilotLockedHeadingImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != SEATALK_PILOT_LOCKED_HEADING_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", SEATALK_PILOT_LOCKED_HEADING_PGN, header.getPgn()));
        fill();
    }

    public N2KSeatalkPilotLockedHeadingImpl(byte[] data) {
        super(getDefaultHeader(SEATALK_PILOT_LOCKED_HEADING_PGN), data);
        fill();
    }

    private void fill() {
        Double d = BitUtils.parseDouble(data, 40, 16, 0.0001, false);
        lockedHeadingMagnetic = (d == null) ? Double.NaN : Utils.round(Math.toDegrees(d), 1);

        d = BitUtils.parseDouble(data, 24, 16, 0.0001, false);
        lockedHeadingTrue = (d == null) ? Double.NaN : Utils.round(Math.toDegrees(d), 1);
    }

    @Override
    public double getLockedHeadingMagnetic() {
        return lockedHeadingMagnetic;
    }

    @Override
    public double getLockedHeadingTrue() {
        return lockedHeadingTrue;
    }
}
