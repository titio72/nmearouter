package com.aboni.nmea.router.track.impl;

import com.aboni.nmea.router.track.TrackAnalytics;
import com.aboni.nmea.router.track.TrackManagementException;
import com.aboni.nmea.router.track.TrackReader;
import com.aboni.utils.db.DBHelper;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

public class DBTrackAnalyticsByDate {

    private static final String SQL = "select TS, dist, speed, maxSpeed, engine, anchor, dTime, lat, lon from track where TS>=? and TS<?";

    public JSONObject getAnalysis(Instant from, Instant to) throws TrackManagementException {
        TrackAnalytics analytics = new TrackAnalytics("");
        TrackReader reader = new DBTrackReader((DBHelper db) -> {
            PreparedStatement st = db.getConnection().prepareStatement(SQL);
            st.setTimestamp(1, new Timestamp(from.toEpochMilli()));
            st.setTimestamp(2, new Timestamp(to.toEpochMilli()));
            return st;
        });
        reader.readTrack(sample -> analytics.processSample(sample));
        return analytics.getJSONStats();
    }

}
