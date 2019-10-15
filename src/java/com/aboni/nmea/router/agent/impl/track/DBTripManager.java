package com.aboni.nmea.router.agent.impl.track;

import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DBTripManager implements TripManager {

    private static final String SQL_GETLASTTRIP = "select max(tripId), max(track.ts) from track where TS>=? and TS<=?";

    @Override
    public Pair<Integer, Long> getCurrentTrip(long now) {
        try (DBHelper db = new DBHelper(false)) {
            try (PreparedStatement st = db.getConnection().prepareStatement(SQL_GETLASTTRIP)) {
                st.setTimestamp(1, new Timestamp(now - (3L * 60L * 60L * 1000L)));
                st.setTimestamp(2, new Timestamp(now));
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        return new Pair<>(rs.getInt(1), rs.getLong(2));
                    }
                }
            }
        } catch (Exception e) {
            ServerLog.getLogger().error("Error detecting current trip", e);
        }
        return null;
    }

    @Override
    public void setTrip(long from, long to, int tripId) {
        try (DBHelper db = new DBHelper(false)) {
            try (PreparedStatement st = db.getConnection().prepareStatement("UPDATE track SET tripId=? WHERE TS>? AND TS<=?")) {
                st.setInt(1, tripId);
                st.setTimestamp(2, new Timestamp(from));
                st.setTimestamp(3, new Timestamp(to));
                st.executeUpdate();
            }
        } catch (Exception e) {
            ServerLog.getLogger().error("Error updating trip", e);
        }
    }
}
