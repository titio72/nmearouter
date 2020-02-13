package com.aboni.nmea.router.services;

import com.aboni.nmea.router.track.DBTrackAnalyticsByDate;
import com.aboni.nmea.router.track.DBTrackAnalyticsByTrip;
import com.aboni.utils.db.DBException;
import com.aboni.nmea.router.track.analytics.TrackAnalytics;

import java.util.Calendar;

public class TrackAnalyticsService extends JSONWebService {

	public TrackAnalyticsService() {
		super();
		setLoader(config -> {
			try {
				TrackAnalytics.Stats stats = null;
				int tripId = config.getInteger("trip", -1);
				if (tripId!=-1) {
					DBTrackAnalyticsByTrip an = new DBTrackAnalyticsByTrip();
					stats = an.run(tripId);
				} else {
					Calendar cFrom = config.getParamAsDate("dateFrom", 0);
					Calendar cTo = config.getParamAsDate("dateTo", 1);
					DBTrackAnalyticsByDate an = new DBTrackAnalyticsByDate();
					stats = an.run(cFrom.toInstant(), cTo.toInstant());
				}
				return stats.toJson();
			} catch (DBException e) {
				throw new JSONGenerationException("Error generating track stats json", e);
			}
		});
	}

}
