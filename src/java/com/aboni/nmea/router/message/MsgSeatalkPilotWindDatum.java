package com.aboni.nmea.router.message;

import com.aboni.utils.JSONUtils;
import org.json.JSONObject;

public interface MsgSeatalkPilotWindDatum extends Message {

    double getRollingAverageWind();

    double getWindDatum();

    @Override
    default JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("topic", "pilot_wind_datum");
        JSONUtils.addDouble(j, getRollingAverageWind(), "rolling_average_wind");
        JSONUtils.addDouble(j, getWindDatum(), "wind_datum");
        return j;
    }
}
