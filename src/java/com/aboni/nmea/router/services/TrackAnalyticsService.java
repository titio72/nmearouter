package com.aboni.nmea.router.services;

import com.aboni.nmea.router.track.TrackAnalyticsByDate;
import com.aboni.nmea.router.track.TrackAnalyticsByTrip;
import com.aboni.nmea.router.track.TrackManagementException;
import com.aboni.nmea.router.track.TripManager;
import com.aboni.utils.ThingsFactory;
import org.json.JSONObject;

import javax.inject.Inject;
import java.time.Instant;

public class TrackAnalyticsService extends JSONWebService {

    @Inject
    public TrackAnalyticsService(final TripManager m) {
        super();
        setLoader((ServiceConfig config) -> {
            try {
                JSONObject stats = null;
                int tripId = config.getInteger("trip", -1);
                if (tripId != -1) {
                    TrackAnalyticsByTrip an = ThingsFactory.getInstance(TrackAnalyticsByTrip.class);
                    stats = an.getAnalysis(tripId);
                } else {
                    TrackAnalyticsByDate an = ThingsFactory.getInstance(TrackAnalyticsByDate.class);
                    Instant cFrom = config.getParamAsInstant("dateFrom", Instant.now().minusSeconds(86400L), -1);
                    Instant cTo = config.getParamAsInstant("dateTo", Instant.now(), 0);
                    stats = an.getAnalysis(cFrom, cTo);
                }
                return stats;
            } catch (TrackManagementException e) {
                throw new JSONGenerationException("Error generating track stats json", e);
            }
        });
    }

}
