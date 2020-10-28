package com.aboni.nmea.router.message;

import org.json.JSONObject;

public interface MsgRudder extends Message {

    int getInstance();

    double getAngle();

    double getAngleOrder();

    int getDirectionOrder();

    @Override
    default JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("topic", "RSA");
        j.put("instance", getInstance());
        if (!Double.isNaN(getAngle())) {
            j.put("angle", getAngle());
            j.put(
                    (getInstance() == 0) ? "starboard_angle" : "port_angle",
                    getAngle());
        }
        return j;
    }
}
