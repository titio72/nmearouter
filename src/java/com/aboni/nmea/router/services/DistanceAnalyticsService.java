package com.aboni.nmea.router.services;

import com.aboni.nmea.router.track.TrackQueryManager;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;

public class DistanceAnalyticsService extends JSONWebService {

    private final TrackQueryManager analytics;

    public DistanceAnalyticsService(@NotNull TrackQueryManager m) {
        super();
        this.analytics = m;
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) throws JSONGenerationException {
        try {
            return analytics.getYearlyStats();
        } catch (Exception e) {
            throw new JSONGenerationException(e);
        }
    }
}
