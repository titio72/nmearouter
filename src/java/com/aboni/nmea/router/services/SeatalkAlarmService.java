package com.aboni.nmea.router.services;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.SeatalkAlarmsStatus;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.message.MsgSeatalkAlarm;
import com.aboni.nmea.router.utils.Log;
import com.aboni.utils.Pair;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SeatalkAlarmService extends JSONWebService {

    private SeatalkAlarmsStatus statusProvider;
    private final NMEARouter router;

    @Inject
    public SeatalkAlarmService(@NotNull NMEARouter router, @NotNull Log log) {
        super(log);
        this.router = router;
        setLoader(this::getResult);
    }

    private void findService() {
        if (statusProvider==null) {
            for (String ag_id : router.getAgents()) {
                NMEAAgent ag = router.getAgent(ag_id);
                if (ag instanceof SeatalkAlarmsStatus) {
                    statusProvider = (SeatalkAlarmsStatus) ag;
                    break;
                }
            }
        }
    }

    private JSONObject getResult(ServiceConfig serviceConfig) {
        JSONObject res = new JSONObject();
        findService();
        if (statusProvider!=null) {
            List<Pair<MsgSeatalkAlarm, Instant>> alarms = new ArrayList<>();
            statusProvider.getAlarms(alarms);
            for (Pair<MsgSeatalkAlarm, Instant> alarm : alarms) {
                JSONObject jA = alarm.first.toJSON();
                jA.put("time", alarm.second.toString());
                res.append("alarms", jA);
            }
        }
        return res;
    }
}
