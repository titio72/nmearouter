package com.aboni.nmea.router.track;

import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class TrackAnalyticsByDate {

    private final TrackReader reader;

    @Inject
    public TrackAnalyticsByDate(@NotNull TrackReader reader) {
        this.reader = reader;
    }

    public JSONObject getAnalysis(Instant from, Instant to) throws TrackManagementException {
        TrackAnalytics analytics = new TrackAnalytics("");
        reader.readTrack(new TrackQueryByDate(from, to), analytics::processSample);
        return analytics.getJSONStats();
    }

}
