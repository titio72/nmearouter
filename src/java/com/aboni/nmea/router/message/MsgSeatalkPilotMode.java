package com.aboni.nmea.router.message;

import org.json.JSONObject;

public interface MsgSeatalkPilotMode extends Message {

    PilotMode getMode();

    @Override
    default JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("topic", "pilot_mode");
        j.put("mode", getMode().toString());
        return j;
    }
}
