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
import com.aboni.nmea.router.message.MsgAttitude;
import com.aboni.nmea.router.message.MsgAttitudeImpl;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.utils.HWSettings;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.ATTITUDE_PGN;

public class N2KAttitudeImpl extends N2KMessageImpl implements MsgAttitude {

    private final MsgAttitude msgAttitude;

    public N2KAttitudeImpl(byte[] data) {
        super(getDefaultHeader(ATTITUDE_PGN), data);
        msgAttitude = fill(data);
    }

    public N2KAttitudeImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != ATTITUDE_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", ATTITUDE_PGN, header.getPgn()));
        msgAttitude = fill(data);
    }

    private static MsgAttitude fill(byte[] data) {
        int sid = BitUtils.getByte(data, 0, 0xFF);

        Double dYaw = BitUtils.parseDouble(data, 8, 16, 0.0001, true);
        double yaw = (dYaw == null) ? Double.NaN : Utils.round(Math.toDegrees(dYaw), 1);

        Double dPitch = BitUtils.parseDouble(data, 24, 16, 0.0001, true);
        double pitch = (dPitch == null) ?
                Double.NaN :
                Utils.round(Math.toDegrees(dPitch), 1) - HWSettings.getPropertyAsDouble("gyro.roll", 0.0);

        Double dRoll = BitUtils.parseDouble(data, 40, 16, 0.0001, true);
        double roll = (dRoll == null) ?
                Double.NaN :
                Utils.round(Math.toDegrees(dRoll), 1) - HWSettings.getPropertyAsDouble("gyro.pitch", 0.0);

        return new MsgAttitudeImpl(sid, yaw, roll, pitch);
    }

    @Override
    public int getSID() {
        return msgAttitude.getSID();
    }

    @Override
    public double getYaw() {
        return msgAttitude.getYaw();
    }

    @Override
    public double getPitch() {
        return msgAttitude.getPitch();
    }

    @Override
    public double getRoll() {
        return msgAttitude.getRoll();
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Roll {%.1f} Yaw {%.1f} Pitch {%.1f}",
                ATTITUDE_PGN, getHeader().getSource(), getRoll(), getYaw(), getPitch());
    }
}
