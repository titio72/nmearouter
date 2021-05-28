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

import com.aboni.nmea.router.message.MsgSeatalkAlarm;
import com.aboni.nmea.router.message.SeatalkAlarm;
import com.aboni.nmea.router.message.SeatalkAlarmStatus;
import com.aboni.nmea.router.message.impl.MsgSeatalkAlarmImpl;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.ENVIRONMENT_PRESSURE_PGN;
import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.SEATALK_ALARM_PGN;

public class N2KSeatalkAlarmImpl extends N2KMessageImpl implements MsgSeatalkAlarm {

    private final MsgSeatalkAlarm msg;

    public N2KSeatalkAlarmImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != SEATALK_ALARM_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", SEATALK_ALARM_PGN, header.getPgn()));
        msg = fill(data, header);
    }

    private static MsgSeatalkAlarm fill(byte[] data, N2KMessageHeader h) {
        int sid = BitUtils.getByte(data, 2);
        SeatalkAlarmStatus alarmStatus = SeatalkAlarmStatus.valueOf(BitUtils.getByte(data, 3));
        SeatalkAlarm alarm = SeatalkAlarm.valueOf(BitUtils.getByte(data, 4));
        int groupId = BitUtils.getByte(data, 5);
        int priority = BitUtils.get2ByteInt(data,6);
        return new MsgSeatalkAlarmImpl(sid, alarmStatus, alarm, groupId, priority, h.getSource());
    }

    @Override
    public int getSID() {
        return msg.getSID();
    }

    @Override
    public SeatalkAlarmStatus getAlarmStatus() {
        return msg.getAlarmStatus();
    }

    @Override
    public SeatalkAlarm getAlarm() {
        return msg.getAlarm();
    }

    @Override
    public int getGroupId() {
        return msg.getGroupId();
    }

    @Override
    public String getGroup() {
        return msg.getGroup();
    }

    @Override
    public int getPriority() {
        return msg.getPriority();
    }

    @Override
    public int getSource() {
        return msg.getSource();
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} AlarmPriority {%d} Alarm {%s} Status {%s} Group {%s}",
                ENVIRONMENT_PRESSURE_PGN, getHeader().getSource(), getPriority(), getAlarm(), getAlarmStatus(), getGroup());
    }
}
