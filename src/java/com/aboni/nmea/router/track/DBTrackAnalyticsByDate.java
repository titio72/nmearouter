package com.aboni.nmea.router.track;

import com.aboni.nmea.router.track.analytics.TrackAnalytics;
import com.aboni.utils.db.DBException;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;

public class DBTrackAnalyticsByDate {

    private static final String SQL = "select TS, dist, speed, maxSpeed, engine, anchor, dTime from track where TS>=? and TS<?";

    public TrackAnalytics.Stats run(Instant from, Instant to) throws DBException {
        TrackAnalytics analytics = new TrackAnalytics();
        TrackReader reader = new TrackReader();
        reader.readTrack(
                sample->analytics.processSample(sample),
                db->{
                    PreparedStatement st = db.getConnection().prepareStatement(SQL);
                    st.setTimestamp(1, new Timestamp(from.toEpochMilli()));
                    st.setTimestamp(2, new Timestamp(to.toEpochMilli()));
                    return st;
                });
        return analytics.getStats();
    }

}
