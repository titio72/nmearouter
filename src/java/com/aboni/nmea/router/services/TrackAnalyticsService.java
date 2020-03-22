package com.aboni.nmea.router.services;

import com.aboni.nmea.router.track.JSONTrackAnalytics;
import com.aboni.nmea.router.track.TrackManagementException;
import com.aboni.utils.Query;
import com.aboni.utils.ThingsFactory;

import javax.inject.Inject;

public class TrackAnalyticsService extends JSONWebService {

    @Inject
    public TrackAnalyticsService() {
        super();
        setLoader((ServiceConfig config) -> {
            try {
                Query q = QueryFactory.getQuery(config);
                JSONTrackAnalytics analytics = ThingsFactory.getInstance(JSONTrackAnalytics.class);
                return analytics.getAnalysis(q);
            } catch (TrackManagementException e) {
                throw new JSONGenerationException("Error generating track stats json", e);
            }
        });
    }

}
