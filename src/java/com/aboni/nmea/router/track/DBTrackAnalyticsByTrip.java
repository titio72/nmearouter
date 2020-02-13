package com.aboni.nmea.router.track;

import com.aboni.nmea.router.track.analytics.TrackAnalytics;
import com.aboni.utils.db.DBException;

import java.sql.PreparedStatement;

public class DBTrackAnalyticsByTrip {

    private static final String SQL = "select TS, dist, speed, maxSpeed, engine, anchor, dTime from track where tripid=?";

    public TrackAnalytics.Stats run(int tripId) throws DBException {
        TrackAnalytics analytics = new TrackAnalytics();
        TrackReader reader = new TrackReader();
        reader.readTrack(
                sample->analytics.processSample(sample),
                db->{
                    PreparedStatement st = db.getConnection().prepareStatement(SQL);
                    st.setInt(1, tripId);
                    return st;
                });
        return analytics.getStats();
    }

}
