package com.aboni.nmea.router.message;

import com.aboni.utils.JSONUtils;
import org.json.JSONObject;

public interface MsgTemperature extends Message {

    int getSID();

    int getInstance();

    TemperatureSource getTemperatureSource();

    double getTemperature();

    double getSetTemperature();

    @Override
    default JSONObject toJSON() {
        JSONObject res = new JSONObject();
        res.put("topic", "temperature");
        res.put("source", getTemperatureSource().toString());
        res.put("instance", getInstance());
        JSONUtils.addDouble(res, getTemperature(), "temperature");
        JSONUtils.addDouble(res, getSetTemperature(), "set_temperature");
        return res;
    }
}