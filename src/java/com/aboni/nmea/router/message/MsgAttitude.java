package com.aboni.nmea.router.message;

import com.aboni.nmea.router.utils.HWSettings;
import com.aboni.utils.JSONUtils;
import org.json.JSONObject;

public interface MsgAttitude extends Message {

    int getSID();

    double getYaw();

    double getPitch();

    double getRoll();

    default @Override
    JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("topic", "attitude");
        JSONUtils.addDouble(json, getYaw(), "yaw");
        JSONUtils.addDouble(json, getRoll() - HWSettings.getPropertyAsDouble("gyro.roll", 0.0), "roll");
        JSONUtils.addDouble(json, getPitch() - HWSettings.getPropertyAsDouble("gyro.pitch", 0.0), "pitch");
        return json;
    }

    default @Override
    String getMessageContentType() {
        return "Attitude";
    }
}
