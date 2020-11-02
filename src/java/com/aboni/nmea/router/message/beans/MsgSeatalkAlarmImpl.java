package com.aboni.nmea.router.message.beans;

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
