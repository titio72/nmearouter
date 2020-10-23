package com.aboni.nmea.router.message;

import com.aboni.utils.JSONUtils;
import org.json.JSONObject;

public interface MsgSeatalkPilotHeading extends Message {

    double getHeadingMagnetic();

    double getHeadingTrue();

    @Override
    default JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("topic", "pilot_heading");
        JSONUtils.addDouble(j, getHeadingMagnetic(), "heading_magnetic");
        JSONUtils.addDouble(j, getHeadingTrue(), "heading_true");
        return j;
    }
}
