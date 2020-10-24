package com.aboni.nmea.router.message;

import com.aboni.utils.JSONUtils;
import org.json.JSONObject;

public interface MsgSeatalkPilotLockedHeading extends Message {

    double getLockedHeadingMagnetic();

    double getLockedHeadingTrue();

    @Override
    default JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("topic", "pilot_locked_heading");
        JSONUtils.addDouble(j, getLockedHeadingMagnetic(), "heading_magnetic");
        JSONUtils.addDouble(j, getLockedHeadingTrue(), "heading_true");
        return j;
    }
}
