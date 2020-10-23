package com.aboni.nmea.router.message;

import com.aboni.utils.JSONUtils;
import org.json.JSONObject;

public interface MsgAttitude extends Message {

    int getSID();

    double getYaw();

    double getPitch();

    double getRoll();

    default @Override JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("topic", "attitude");
        JSONUtils.addDouble(json, getYaw(), "yaw");
        JSONUtils.addDouble(json, getRoll(), "roll");
        JSONUtils.addDouble(json, getPitch(), "pitch");
        return json;
    }
}
