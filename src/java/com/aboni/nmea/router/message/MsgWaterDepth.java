package com.aboni.nmea.router.message;

import com.aboni.utils.JSONUtils;
import org.json.JSONObject;

public interface MsgWaterDepth extends Message {

    int getSID();

    double getDepth();

    double getOffset();

    double getRange();

    @Override
    default JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("topic", "DPT");
        double d = getDepth();
        double o = getOffset();
        if (JSONUtils.addDouble(json, d, "raw_depth") && JSONUtils.addDouble(json, o, "offset")) {
            JSONUtils.addDouble(json, d + o, "depth");
        }
        JSONUtils.addDouble(json, getRange(), "range");
        return json;
    }
}
