package com.aboni.nmea.router.message;

import com.aboni.misc.Utils;
import org.json.JSONObject;

public interface MsgSeatalkPilotWindDatum extends Message {

    double getRollingAverageWind();

    double getWindDatum();

    @Override
    default JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("topic", "pilot_wind_datum");
        if (!Double.isNaN(getWindDatum())) {
            j.put("wind_datum", Math.round(getWindDatum()));
            j.put("wind_datum_view", Math.abs(Utils.normalizeDegrees180To180(Math.round(getWindDatum()))));
            j.put("wind_datum_side", getWindDatum()>180.0?"P":"S");
        }
        if (!Double.isNaN(getRollingAverageWind())) {
            j.put("rolling_average_wind", Math.round(getRollingAverageWind()));
            j.put("rolling_average_wind_view", Math.abs(Utils.normalizeDegrees180To180(Math.round(getRollingAverageWind()))));
            j.put("rolling_average_wind_side", getRollingAverageWind()>180.0?"P":"S");
        }
        return j;
    }
}
