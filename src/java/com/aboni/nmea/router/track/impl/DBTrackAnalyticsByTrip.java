package com.aboni.nmea.router.track.impl;

import com.aboni.nmea.router.track.TrackAnalytics;
import com.aboni.nmea.router.track.TrackManagementException;
import com.aboni.nmea.router.track.TrackReader;
import com.aboni.nmea.router.track.TripManager;
import com.aboni.utils.db.DBHelper;
import org.json.JSONObject;

import java.sql.PreparedStatement;

public class DBTrackAnalyticsByTrip {

    private static final String SQL = "select TS, dist, speed, maxSpeed, engine, anchor, dTime, lat, lon from track where tripid=?";
    private final TripManager tripManager;

    public DBTrackAnalyticsByTrip(TripManager m) {
        tripManager = m;
    }

    public DBTrackAnalyticsByTrip() {
        tripManager = null;
    }

    public JSONObject getAnalysis(int tripId) throws TrackManagementException {
        TrackAnalytics analytics = new TrackAnalytics((tripManager != null) ? tripManager.getTripName(tripId) : "");
        TrackReader reader = new DBTrackReader(
                (DBHelper db) -> {
                    PreparedStatement st = db.getConnection().prepareStatement(SQL);
                    st.setInt(1, tripId);
                    return st;
                });
        reader.readTrack(
                sample -> analytics.processSample(sample));
        return analytics.getJSONStats();
    }

}
