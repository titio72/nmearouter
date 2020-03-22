package com.aboni.nmea.router.services;

import com.aboni.nmea.router.track.TrackQueryManager;
import com.aboni.utils.ServerLog;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class YearlyAnalyticsService extends JSONWebService {

    private final TrackQueryManager trackQueryManager;

    @Inject
    public YearlyAnalyticsService(@NotNull final TrackQueryManager manager) {
        super();
        this.trackQueryManager = manager;
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) throws JSONGenerationException {
        try {
            return trackQueryManager.getYearlyStats();
        } catch (Exception e) {
            ServerLog.getLogger().error("Error reading yearly stats", e);
            throw new JSONGenerationException("Error reading yearly stats", e);
        }
    }
}