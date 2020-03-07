package com.aboni.nmea.router.services;

import com.aboni.nmea.router.track.TrackManagementException;
import com.aboni.nmea.router.track.TripManager;
import com.aboni.nmea.router.track.impl.DBTrackAnalyticsByDate;
import com.aboni.nmea.router.track.impl.DBTrackAnalyticsByTrip;
import org.json.JSONObject;

import java.util.Calendar;

public class TrackAnalyticsService extends JSONWebService {

    public TrackAnalyticsService(final TripManager m) {
        super();
        setLoader((ServiceConfig config) -> {
            try {
                JSONObject stats = null;
                int tripId = config.getInteger("trip", -1);
                if (tripId != -1) {
                    DBTrackAnalyticsByTrip an = new DBTrackAnalyticsByTrip(m);
                    stats = an.getAnalysis(tripId);
                } else {
                    Calendar cFrom = config.getParamAsDate("dateFrom", 0);
                    Calendar cTo = config.getParamAsDate("dateTo", 1);
                    DBTrackAnalyticsByDate an = new DBTrackAnalyticsByDate();
                    stats = an.getAnalysis(cFrom.toInstant(), cTo.toInstant());
                }
                return stats;
            } catch (TrackManagementException e) {
                throw new JSONGenerationException("Error generating track stats json", e);
            }
		});
	}

}
