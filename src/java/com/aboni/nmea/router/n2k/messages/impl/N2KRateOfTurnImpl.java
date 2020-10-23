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

import com.aboni.misc.Utils;
import com.aboni.nmea.router.message.MsgRateOfTurn;
import com.aboni.nmea.router.message.MsgRateOfTurnImpl;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import org.json.JSONObject;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.RATE_OF_TURN_PGN;

public class N2KRateOfTurnImpl extends N2KMessageImpl implements MsgRateOfTurn {

    private final MsgRateOfTurn msg;

    public N2KRateOfTurnImpl(byte[] data) {
        super(getDefaultHeader(RATE_OF_TURN_PGN), data);
        msg = fill(data);
    }

    public N2KRateOfTurnImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != RATE_OF_TURN_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", RATE_OF_TURN_PGN, header.getPgn()));
        msg = fill(data);
    }

    private static MsgRateOfTurn fill(byte[] data) {
        int sid = BitUtils.getByte(data, 0, 0xFF);

        Double dRate = BitUtils.parseDouble(data, 8, 32, 3.125e-08, true);
        double rate = dRate == null ? Double.NaN : Utils.round(Math.toDegrees(dRate), 4);

        return new MsgRateOfTurnImpl(sid, rate);
    }

    @Override
    public int getSID() {
        return msg.getSID();
    }

    @Override
    public double getRateOfTurn() {
        return msg.getRateOfTurn();
    }

    @Override
    public String toString() {
        return String.format("PGN {%d} Source {%d} RateOfTurn {%.1f}", getHeader().getPgn(), getHeader().getSource(), getRateOfTurn());
    }

    @Override
    public JSONObject toJSON() {
        return msg.toJSON();
    }
}
