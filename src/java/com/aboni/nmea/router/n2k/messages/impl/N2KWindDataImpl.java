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
import com.aboni.nmea.router.message.MsgWindData;
import com.aboni.nmea.router.message.impl.MsgWindDataImpl;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.WIND_PGN;

public class N2KWindDataImpl extends N2KMessageImpl implements MsgWindData {

    private final MsgWindData windData;

    public N2KWindDataImpl(byte[] data) {
        super(getDefaultHeader(WIND_PGN), data);
        windData = fill(data);
    }

    public N2KWindDataImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != WIND_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", WIND_PGN, header.getPgn()));
        windData = fill(data);
    }

    private static MsgWindData fill(byte[] data) {
        int sid = BitUtils.getByte(data, 0, 0xFF);

        Double dSpeed = BitUtils.parseDouble(data, 8, 16, 0.01, false);
        double speed = (dSpeed != null) ? Utils.round(dSpeed * 3600.0 / 1852.0, 2) : Double.NaN;

        Double dAngleRad = BitUtils.parseDouble(data, 24, 16, 0.0001, false);
        double angle = (dAngleRad != null) ? Utils.round(Math.toDegrees(dAngleRad), 1) : Double.NaN;

        boolean apparent = (BitUtils.getByte(data, 5, 1) & 0x07) != 0;

        return new MsgWindDataImpl(sid, speed, angle, apparent);
    }

    @Override
    public int getSID() {
        return windData.getSID();
    }

    @Override
    public double getSpeed() {
        return windData.getSpeed();
    }

    @Override
    public double getAngle() {
        return windData.getAngle();
    }

    @Override
    public boolean isApparent() {
        return windData.isApparent();
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Wind Speed {%.1f} Angle {%.1f} Ref {%s}",
                WIND_PGN, getHeader().getSource(), getSpeed(), getAngle(), isApparent() ? "A" : "T");
    }
}
