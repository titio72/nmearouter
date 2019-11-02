package com.aboni.nmea.router.agent.impl.track;

import com.aboni.geo.GeoPositionT;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

public class DBTripManager implements TripManager {

    static String sTABLE = "track";

    public List<TrackPoint> loadTrip(int trip) {
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement st = db.getConnection().prepareStatement("select * from track where tripId=?")) {
                st.setTimestamp(1, new Timestamp(trip));
                ResultSet rs = st.executeQuery();
                List<TrackPoint> res = new LinkedList<>();
                while (rs.next()) {
                    TrackPoint p = new TrackPoint(
                            new GeoPositionT(rs.getTimestamp("TS").getTime(), rs.getDouble("lat"), rs.getDouble("lon")),
                            rs.getBoolean("anchor"),
                            rs.getDouble("dist"),
                            rs.getDouble("speed"),
                            rs.getDouble("maxSpeed"),
                            rs.getInt("dTime"),
                            trip);
                    res.add(p);
                }
                return res;
            }
        } catch (Exception e) {
            ServerLog.getLogger().error("Error detecting current trip", e);
        }
        return null;
    }

    @Override
    public Pair<Integer, Long> getCurrentTrip(long now) {
        try (DBHelper db = new DBHelper(false)) {
            try (PreparedStatement st = db.getConnection().prepareStatement("select max(tripId), max(TS) from " + sTABLE + " where TS>=? and TS<=? AND tripId is not null")) {
                st.setTimestamp(1, new Timestamp(now - (3L * 60L * 60L * 1000L)));
                st.setTimestamp(2, new Timestamp(now));
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        return new Pair<>(rs.getInt(1), rs.getTimestamp(2).getTime());
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
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement st = db.getConnection().prepareStatement("UPDATE " + sTABLE + " SET tripId=? WHERE TS>? AND TS<=? AND tripId is null")) {
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
