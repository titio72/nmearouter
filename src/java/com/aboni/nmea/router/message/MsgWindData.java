package com.aboni.nmea.router.message;

import org.json.JSONObject;

public interface MsgWindData extends Message {

    int getSID();

    double getSpeed();

    double getAngle();

    boolean isApparent();

    default boolean isTrue() {
        return !isApparent();
    }

    @Override
    default JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("topic", isTrue() ? "MWV_T" : "MWV_R");
        json.put("angle", getAngle());
        json.put("speed", getSpeed());
        return json;
    }
}
