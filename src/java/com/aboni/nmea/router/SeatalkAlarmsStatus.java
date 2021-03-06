package com.aboni.nmea.router;

import com.aboni.nmea.router.message.MsgSeatalkAlarm;
import com.aboni.nmea.router.message.SeatalkAlarm;
import com.aboni.utils.Pair;

import java.time.Instant;
import java.util.List;

public interface SeatalkAlarmsStatus {

    interface AlarmListener {
        void onAlarm(MsgSeatalkAlarm alarm, Instant time);
    }

    void getAlarms(List<Pair<MsgSeatalkAlarm, Instant>> alarms);

    void subscribe(AlarmListener listener);

    void unsubscribe(AlarmListener listener);

    MsgSeatalkAlarm getAlarm(int source, SeatalkAlarm alarm);

}
