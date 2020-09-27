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
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.messages.N2KAttitude;

import static com.aboni.nmea.router.n2k.messages.N2kMessagePGNs.ATTITUDE_PGN;

public class N2KAttitudeImpl extends N2KMessageImpl implements N2KAttitude {

    private int sid;
    private double yaw;
    private double pitch;
    private double roll;

    public N2KAttitudeImpl(byte[] data) {
        super(getDefaultHeader(ATTITUDE_PGN), data);
        fill();
    }

    public N2KAttitudeImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != ATTITUDE_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", ATTITUDE_PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        sid = getByte(data, 0, 0xFF);

        Double dYaw = parseDouble(data, 8, 16, 0.0001, true);
        yaw = (dYaw == null) ? Double.NaN : Utils.round(Math.toDegrees(dYaw), 1);

        Double dPitch = parseDouble(data, 24, 16, 0.0001, true);
        pitch = (dPitch == null) ? Double.NaN : Utils.round(Math.toDegrees(dPitch), 1);

        Double dRoll = parseDouble(data, 40, 16, 0.0001, true);
        roll = (dRoll == null) ? Double.NaN : Utils.round(Math.toDegrees(dRoll), 1);
    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public double getYaw() {
        return yaw;
    }

    @Override
    public double getPitch() {
        return pitch;
    }

    @Override
    public double getRoll() {
        return roll;
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Roll {%.1f} Yaw {%.1f} Pitch {%.1f}",
                ATTITUDE_PGN, getHeader().getSource(), getRoll(), getYaw(), getPitch());
    }
}
