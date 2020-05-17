package com.aboni.nmea.router.data.meteo.impl;

import com.aboni.nmea.router.data.meteo.Meteo;
import com.aboni.nmea.router.data.meteo.MeteoManagementException;
import com.aboni.nmea.router.data.meteo.MeteoReader;
import com.aboni.nmea.router.data.meteo.MeteoSample;
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
        m.readMeteo(from, to, (MeteoSample sample) -> addSample(sample, res));
        return res;
    }

    private void addSample(MeteoSample sample, JSONObject res) {
        JSONArray a;
        if (res.has(sample.getTag())) {
            a = res.getJSONArray(sample.getTag());
        } else {
            a = new JSONArray();
            res.put(sample.getTag(), a);
        }
        JSONObject s = new JSONObject();
        s.put("time", sample.getTs());
        s.put("vMin", sample.getMinValue());
        s.put("v", sample.getValue());
        s.put("vMax", sample.getMaxValue());
        a.put(s);
    }
}
