package com.aboni.nmea.router.meteo.impl;

import com.aboni.nmea.router.meteo.Meteo;
import com.aboni.nmea.router.meteo.MeteoManagementException;
import com.aboni.nmea.router.meteo.MeteoReader;
import com.aboni.nmea.router.meteo.MeteoSample;
import com.aboni.utils.ThingsFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class DBMeteo implements Meteo {

    public DBMeteo() {
        // nothing to initialize
    }

    @Override
    public JSONObject getMeteoSeries(@NotNull Instant from, @NotNull Instant to) throws MeteoManagementException {
        MeteoReader m = ThingsFactory.getInstance(MeteoReader.class);
        JSONObject res = new JSONObject();
        m.readMeteo(from, to, (MeteoSample sample) -> addSample(sample.getTag(), sample.getTs(), sample.getMinValue(), sample.getValue(), sample.getMaxValue(), res));
        return res;
    }


    private void addSample(String type, long t, double vMin, double v, double vMax, JSONObject res) {
        JSONArray a;
        if (res.has(type)) {
            a = res.getJSONArray(type);
        } else {
            a = new JSONArray();
            res.put(type, a);
        }
        JSONObject s = new JSONObject();
        s.put("time", t);
        s.put("vMin", vMin);
        s.put("v", v);
        s.put("vMax", vMax);
        a.put(s);
    }
}
