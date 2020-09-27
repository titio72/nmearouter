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

import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.PilotMode;
import com.aboni.nmea.router.n2k.SeatalkPilotMode;
import com.aboni.nmea.router.n2k.messages.N2KSeatalkPilotMode;

import static com.aboni.nmea.router.n2k.messages.N2kMessagePGNs.SEATALK_PILOT_MODE_PGN;

public class N2KSeatalkPilotModeImpl extends N2KMessageImpl implements N2KSeatalkPilotMode {

    private SeatalkPilotMode mode;

    public N2KSeatalkPilotModeImpl(byte[] data) {
        super(getDefaultHeader(SEATALK_PILOT_MODE_PGN), data);
        fill();
    }

    public N2KSeatalkPilotModeImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != SEATALK_PILOT_MODE_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", SEATALK_PILOT_MODE_PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        int m = (int) parseIntegerSafe(data, 16, 0, 8, 0xFF);
        int sm = (int) parseIntegerSafe(data, 16, 0, 8, 0xFF);
        int d = (int) parseIntegerSafe(data, 16, 0, 8, 0xFF);
        mode = new SeatalkPilotMode(m, sm, d);
    }

    @Override
    public PilotMode getMode() {
        return mode.getPilotMode();
    }

    public void getMode(PilotMode m) {
        mode.setPilotMode(m);
    }

    @Override
    public String toString() {
        return String.format("PGN {%d} Source {%d} Mode {%s}",
                getHeader().getPgn(), getHeader().getSource(), getMode().toString());
    }
}
