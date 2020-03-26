package com.aboni.nmea.router.services;

import com.aboni.nmea.router.data.track.JSONTrackAnalytics;
import com.aboni.nmea.router.data.track.TrackManagementException;
import com.aboni.utils.Query;

import javax.inject.Inject;

public class TrackAnalyticsService extends JSONWebService {

    private @Inject
    QueryFactory queryFactory;
    private @Inject
    JSONTrackAnalytics analytics;

    @Inject
    public TrackAnalyticsService() {
        super();
        setLoader((ServiceConfig config) -> {
            try {
                Query q = queryFactory.getQuery(config);
                return analytics.getAnalysis(q);
            } catch (TrackManagementException e) {
                throw new JSONGenerationException("Error generating track stats json", e);
            }
        });
    }

}
