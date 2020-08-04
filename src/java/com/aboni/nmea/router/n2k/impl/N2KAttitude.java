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

package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

public class N2KAttitude extends N2KMessageImpl {

    private static final int PGN = 127257;

    private int sid;
    private double yaw;
    private double pitch;
    private double roll;

    public N2KAttitude(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KAttitude(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        sid = getByte(data, 0, 0xFF);

        Double dYaw = parseDouble(data, 8, 0, 16, 0.0001, true);
        yaw = (dYaw == null) ? Double.NaN : Utils.round(Math.toDegrees(dYaw), 1);

        Double dPitch = parseDouble(data, 24, 0, 16, 0.0001, true);
        pitch = (dPitch == null) ? Double.NaN : Utils.round(Math.toDegrees(dPitch), 1);

        Double dRoll = parseDouble(data, 40, 0, 16, 0.0001, true);
        roll = (dRoll == null) ? Double.NaN : Utils.round(Math.toDegrees(dRoll), 1);
    }

    public int getSID() {
        return sid;
    }

    public double getYaw() {
        return yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public double getRoll() {
        return roll;
    }

}
