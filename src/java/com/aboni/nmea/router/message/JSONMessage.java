package com.aboni.nmea.router.message;

import com.aboni.nmea.message.Message;
import org.json.JSONObject;

public class JSONMessage implements Message {

    private final JSONObject payload;

    public JSONMessage(JSONObject payload) {
        if (payload==null) throw new NullPointerException("Message payload cannot be null");
        this.payload = payload;
    }

    @Override
    public JSONObject toJSON() {
        return payload;
    }

    @Override
    public String getMessageContentType() {
        if (payload.has("topic"))
            return payload.getString("topic");
        else
            return null;
    }
}
