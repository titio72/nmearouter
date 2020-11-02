package com.aboni.nmea.router.message;

import com.aboni.utils.JSONUtils;
import org.json.JSONObject;

public interface MsgSeatalkAlarm extends Message {
    int getSID();

    SeatalkAlarmStatus getAlarmStatus();

    SeatalkAlarm getAlarm();

    int getGroupId();

    String getGroup();

    int getPriority();

    int getSource();

    @Override
    default JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("topic", "stalk_alarm");
        j.put("source", getSource());
        j.put("priority", getPriority());
        j.put("groupId", getGroupId());
        JSONUtils.addString(j, getGroup(), "group");
        if (getAlarm()!=null) {
            j.put("alarm", getAlarm().toString());
            j.put("alarm_id", getAlarm().getValue());
        }
        return j;
    }
}
