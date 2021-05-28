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

package com.aboni.nmea.router.message.impl;

import com.aboni.nmea.router.message.MsgSeatalkAlarm;
import com.aboni.nmea.router.message.SeatalkAlarm;
import com.aboni.nmea.router.message.SeatalkAlarmStatus;
import com.aboni.nmea.router.n2k.N2KLookupTables;

public class MsgSeatalkAlarmImpl implements MsgSeatalkAlarm {

    private final int sid;
    private final SeatalkAlarmStatus alarmStatus;
    private final SeatalkAlarm alarm;
    private final int groupId;
    private final String group;
    private final int priority;
    private final int src;

    public MsgSeatalkAlarmImpl(int sid, SeatalkAlarmStatus status, SeatalkAlarm alarm, int group, int priority, int src) {
        this.sid = sid;
        this.alarmStatus = status;
        this.alarm = alarm;
        this.groupId = group;
        this.group = N2KLookupTables.getTable(N2KLookupTables.LOOKUP_MAPS.SEATALK_ALARM_GROUP).getOrDefault(groupId, null);
        this.priority = priority;
        this.src = src;
    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public SeatalkAlarmStatus getAlarmStatus() {
        return alarmStatus;
    }

    @Override
    public SeatalkAlarm getAlarm() {
        return alarm;
    }

    @Override
    public int getGroupId() {
        return groupId;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public int getSource() {
        return src;
    }
}
