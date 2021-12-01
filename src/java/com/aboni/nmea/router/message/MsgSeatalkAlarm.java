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
        j.put("status", getAlarmStatus());
        j.put("priority", getPriority());
        j.put("groupId", getGroupId());
        JSONUtils.addString(j, getGroup(), "group");
        if (getAlarm() != null) {
            j.put("alarm", getAlarm().toString());
            j.put("alarm_id", getAlarm().getValue());
        }
        return j;
    }

    @Override
    default String getMessageContentType() {
        return "SealtalkAlarm";
    }

}
