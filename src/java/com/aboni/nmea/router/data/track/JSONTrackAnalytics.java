package com.aboni.nmea.router.data.track;

import com.aboni.utils.Query;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class JSONTrackAnalytics {

    private final TrackReader reader;

    @Inject
    public JSONTrackAnalytics(@NotNull TrackReader reader) {
        this.reader = reader;
    }

    public JSONObject getAnalysis(@NotNull Query query) throws TrackManagementException {
        TrackAnalytics analytics = new TrackAnalytics("");
        reader.readTrack(query, analytics::processSample);
        return analytics.getJSONStats();
    }

}
