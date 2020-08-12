package com.aboni.nmea.router.data.meteo.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.data.meteo.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DBWindSTatsReader implements WindStatsReader {

    private MeteoReader reader;

    private double wSpeed;
    private double wAngle;
    private long wSpeedTime;
    private long wAngleTime;

    @Inject
    public DBWindSTatsReader(@NotNull MeteoReader reader) {
        this.reader = reader;
    }

    @Override
    public JSONObject getWindStats(Instant from, Instant to, int sectors) throws MeteoManagementException {
        if (360 % sectors != 0) throw new MeteoManagementException("Number of sectors must divide 360");
        WindStats stats = new WindStats(sectors);
        reader.readMeteo(from, to, (MeteoSample sample) -> {
                    switch (sample.getTag()) {
                        case "TW_":
                            if ((sample.getTs() - wAngleTime) < 250) {
                                stats.addSample(60, wAngle, sample.getValue());
                            } else {
                                wSpeed = sample.getValue();
                                wSpeedTime = sample.getTs();
                            }
                            break;
                        case "TWD":
                            if ((sample.getTs() - wSpeedTime) < 250) {
                                stats.addSample(60, sample.getValue(), wSpeed);
                            } else {
                                wAngle = sample.getValue();
                                wAngleTime = sample.getTs();
                            }
                            break;
                    }
                }
        );
        JSONObject res = new JSONObject();
        List<JSONObject> l = new ArrayList<>(360);
        for (int i = 0; i < sectors; i++) {
            JSONObject sample = new JSONObject();
            sample.put("angle", i * (360 / sectors));
            sample.put("windDistance", Utils.round(stats.getWindDistance(i), 1));
            sample.put("windDistanceH", Utils.round(stats.getWindDistance(i) / (stats.getTotalTime() / 3600.0), 1));
            l.add(sample);
        }
        res.put("values", new JSONArray(l));
        res.put("interval", stats.getTotalTime());
        res.put("maxValue", Utils.round(stats.getMaxWindDistance(), 1));
        res.put("maxValueH", Utils.round(stats.getMaxWindDistance() / (stats.getTotalTime() / 3600.0), 1));
        res.put("tot", stats.getTot());
        return res;
    }
}
