package com.aboni.nmea.router.services;

import com.aboni.utils.db.DBException;
import com.aboni.utils.db.TrackAnalytics;

import java.util.Calendar;

public class TrackAnalyticsService extends JSONWebService {

	public TrackAnalyticsService() {
		super();
		setLoader(config -> {
			Calendar cFrom = config.getParamAsDate("dateFrom", 0);
			Calendar cTo = config.getParamAsDate("dateTo", 1);

			TrackAnalytics an = new TrackAnalytics();
			try {
				TrackAnalytics.Stats s = an.run(cFrom.toInstant(), cTo.toInstant());
				return s.toJson();
			} catch (DBException e) {
				throw new RuntimeException(e);
			}
		});
	}

}
