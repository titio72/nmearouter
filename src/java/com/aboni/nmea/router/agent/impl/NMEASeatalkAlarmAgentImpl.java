package com.aboni.nmea.router.agent.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.SeatalkAlarmsStatus;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.message.MsgSeatalkAlarm;
import com.aboni.nmea.router.message.SeatalkAlarm;
import com.aboni.nmea.router.message.SeatalkAlarmStatus;
import com.aboni.utils.Log;
import com.aboni.utils.Pair;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.*;

public class NMEASeatalkAlarmAgentImpl extends NMEAAgentImpl implements SeatalkAlarmsStatus {

    private static class AlarmId {

        final int id;

        AlarmId(int s, SeatalkAlarm a) {
            id = a.getValue() << 8 + s;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof AlarmId)
                return id==((AlarmId)obj).id;
            else
                return false;
        }
    }

    private final TimestampProvider tp;
    private final Log log;
    private final Map<AlarmId, Pair<MsgSeatalkAlarm, Instant>> alarms;
    private final Set<AlarmListener> listeners;
    private static final long CLEANUP_TIMEOUT = 300000;

    @Inject
    public NMEASeatalkAlarmAgentImpl(@NotNull Log log, @NotNull TimestampProvider tp) {
        super(log, tp, false, true);
        this.log = log;
        this.tp = tp;
        this.alarms = new HashMap<>();
        this.listeners = new HashSet<>();
    }

    @OnRouterMessage
    public void onMessage(RouterMessage routerMessage) {
        if (routerMessage.getMessage() instanceof MsgSeatalkAlarm) {
            Instant time = Instant.ofEpochMilli(routerMessage.getTimestamp());
            MsgSeatalkAlarm seatalkAlarm = (MsgSeatalkAlarm) routerMessage.getMessage();
            getLogBuilder().wO("append alarm").wV("alarm", seatalkAlarm).info(log);
            synchronized (alarms) {
                alarms.put(
                        new AlarmId(seatalkAlarm.getSource(), seatalkAlarm.getAlarm()),
                        new Pair<>(seatalkAlarm, time));
            }
            synchronized (listeners) {
                for (AlarmListener listener: listeners) {
                    try {
                        listener.onAlarm(seatalkAlarm, time);
                    } catch (Exception e) {
                        getLogBuilder().wO("dispatch alarm").wV("listener", listener.toString()).error(log, e);
                    }
                }
            }
        }
    }

    @Override
    public void getAlarms(List<Pair<MsgSeatalkAlarm, Instant>> list) {
        synchronized (alarms) {
            list.clear();
            list.addAll(alarms.values());
        }
    }

    @Override
    public void subscribe(AlarmListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void unsubscribe(AlarmListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public MsgSeatalkAlarm getAlarm(int source, SeatalkAlarm alarm) {
        return alarms.getOrDefault(new AlarmId(source, alarm), new Pair<>(null, null)).first;
    }

    @Override
    public String getType() {
        return "Seatalk Alarms";
    }

    @Override
    public String getDescription() {
        return getType();
    }

    @Override
    public void onTimer() {
        super.onTimer();
        synchronized (alarms) {
            long now = tp.getNow();
            alarms.entrySet().removeIf(
                    e -> e.getValue().first.getAlarmStatus()==SeatalkAlarmStatus.OFF &&
                            Utils.isOlderThan(e.getValue().second.toEpochMilli(), now, CLEANUP_TIMEOUT)
            );
        }
    }
}
