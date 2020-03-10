package com.aboni.nmea.router.track;

import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class TrackAnalyticsByTrip {

    private final TrackReader reader;
    private final TripManager tripManager;

    @Inject
    public TrackAnalyticsByTrip(@NotNull TripManager m, @NotNull TrackReader r) {
        this.reader = r;
        this.tripManager = m;
    }

    public JSONObject getAnalysis(int tripId) throws TrackManagementException {
        TrackAnalytics analytics = new TrackAnalytics(tripManager.getTripName(tripId));
        reader.readTrack(new TrackQueryById(tripId), analytics::processSample);
        return analytics.getJSONStats();
    }

}
