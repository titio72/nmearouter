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
import com.aboni.nmea.router.message.MsgRudder;
import com.aboni.nmea.router.message.impl.MsgRudderImpl;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.RUDDER_PGN;

public class N2KRudderImpl extends N2KMessageImpl implements MsgRudder {

    private final MsgRudder msg;

    public N2KRudderImpl(byte[] data) {
        super(getDefaultHeader(RUDDER_PGN), data);
        msg = fill(data);
    }

    public N2KRudderImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != RUDDER_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", RUDDER_PGN, header.getPgn()));
        msg = fill(data);
    }

    private static MsgRudder fill(byte[] data) {

        int instance = BitUtils.getByte(data, 0, 0xFF);

        Long i = BitUtils.parseInteger(data, 8, 2);
        int directionOrder = (i == null) ? -1 : i.intValue();

        Double dAO = BitUtils.parseDouble(data, 16, 16, 0.0001, true);
        double angleOrder = dAO == null ? Double.NaN : Utils.round(Math.toDegrees(dAO), 1);

        Double dP = BitUtils.parseDouble(data, 32, 16, 0.0001, true);
        double position = dP == null ? Double.NaN : Utils.round(Math.toDegrees(dP), 1);

        return new MsgRudderImpl(instance, position, angleOrder, directionOrder);
    }

    @Override
    public int getInstance() {
        return msg.getInstance();
    }

    @Override
    public double getAngle() {
        return msg.getAngle();
    }

    @Override
    public double getAngleOrder() {
        return msg.getAngleOrder();
    }

    @Override
    public int getDirectionOrder() {
        return msg.getDirectionOrder();
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d}  Rudder Instance {%d} Position {%.1f} Angle Order {%.1f} Direction Order {%d}",
                RUDDER_PGN, getHeader().getSource(), getInstance(), getAngle(), getAngleOrder(), getDirectionOrder());
    }
}
