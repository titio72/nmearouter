package com.aboni.nmea.router.message;

import com.aboni.utils.JSONUtils;
import org.json.JSONObject;

public interface MsgHeading extends Message {

    int getSID();

    double getHeading();

    double getDeviation();

    double getVariation();

    DirectionReference getReference();

    boolean isTrueHeading();

    @Override
    default JSONObject toJSON() {
        if (!Double.isNaN(getHeading())) {
            JSONObject json = new JSONObject();
            json.put("topic", "heading");
            JSONUtils.addDouble(json, getHeading(), "heading");
            JSONUtils.addDouble(json, getVariation(), "variation");
            JSONUtils.addDouble(json, getDeviation(), "deviation");
            json.put("reference", getReference().toString());
            return json;
        } else {
            return null;
        }
    }
}
