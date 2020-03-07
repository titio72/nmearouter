package com.aboni.nmea.router.track.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.track.TrackPoint;
import com.aboni.nmea.router.track.TripManager;
import com.aboni.nmea.router.track.TripManagerException;
import com.aboni.sensors.EngineStatus;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;

import java.sql.*;
import java.util.*;

public class DBTripManager implements TripManager {

    static String trackTable = "track";
    static String tripTable = "trip";

    static Map<Integer, String> trips = new HashMap<>();
    static boolean tripsLoaded;

    @Override
    public String getTripName(int tripId) {
        synchronized (trips) {
            if (!tripsLoaded) {
                try (DBHelper db = new DBHelper(false)) {
                    try (ResultSet rs = db.getConnection().createStatement().executeQuery("select * from " + tripTable)) {
                        while (rs.next()) {
                            trips.put(rs.getInt(1), rs.getString(2));
                        }
                    }
                } catch (SQLException | ClassNotFoundException e) {
                    ServerLog.getLogger().error("Error caching trip names", e);
                }
            }
            return trips.getOrDefault(tripId, null);
        }
    }

    @Override
    public List<TrackPoint> loadTrip(int trip) throws TripManagerException {
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement st = db.getConnection().prepareStatement("select * from " + trackTable + " where tripId=?")) {
                st.setTimestamp(1, new Timestamp(trip));
                try (ResultSet rs = st.executeQuery()) {
                    List<TrackPoint> res = new LinkedList<>();
                    while (rs.next()) {
                        TrackPoint p = TrackPoint.newInstance(
                                new GeoPositionT(rs.getTimestamp("TS").getTime(), rs.getDouble("lat"), rs.getDouble("lon")),
                                rs.getBoolean("anchor"),
                                rs.getDouble("dist"),
                                rs.getDouble("speed"),
                                rs.getDouble("maxSpeed"),
                                rs.getInt("dTime"),
                                EngineStatus.valueOf(rs.getByte("engine")),
                                trip);
                        res.add(p);
                    }
                    return res;
                }
            }
        } catch (Exception e) {
            throw new TripManagerException("Error loading trip", e);
        }
    }

    @Override
    public Pair<Integer, Long> getCurrentTrip(long now) throws TripManagerException {
        try (DBHelper db = new DBHelper(false)) {
            try (PreparedStatement st = db.getConnection().prepareStatement("select max(tripId), max(TS) from " +
                    trackTable + " where TS>=? and TS<=? AND tripId is not null")) {
                st.setTimestamp(1, new Timestamp(now - (3L * 60L * 60L * 1000L)));
                st.setTimestamp(2, new Timestamp(now));
                try (ResultSet rs = st.executeQuery()) {
                    if (rs.next()) {
                        return new Pair<>(rs.getInt(1), rs.getTimestamp(2).getTime());
                    } else {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            throw new TripManagerException("Error detecting current trip", e);
        }
    }

    @Override
    public void setTrip(long from, long to, int tripId) throws TripManagerException {
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement st = db.getConnection().prepareStatement("UPDATE " + trackTable +
                    " SET tripId=? WHERE TS>? AND TS<=? AND tripId is null")) {
                st.setInt(1, tripId);
                st.setTimestamp(2, new Timestamp(from));
                st.setTimestamp(3, new Timestamp(to));
                st.executeUpdate();
            }
        } catch (Exception e) {
            throw new TripManagerException("Error updating trip", e);
        }
    }

    @Override
    public int createTrip() throws TripManagerException {
        try (DBHelper h = new DBHelper(true)) {
            int i;
            try (Statement st = h.getConnection().createStatement()) {
                try (ResultSet rs = st.executeQuery("select max(id) from trip")) {
                    if (rs.next()) {
                        i = rs.getInt(1) + 1;
                    } else {
                        i = 1;
                    }
                }
                try (PreparedStatement st1 = h.getConnection().prepareStatement("insert into " + tripTable + " (id, description) values (?, ?)")) {
                    String desc = "Trip " + i;
                    st1.setInt(1, i);
                    st1.setString(2, desc);
                    if (st1.executeUpdate() == 1) {
                        synchronized (trips) {
                            trips.put(i, desc);
                        }
                    }
                }
            }
            return i;
        } catch (Exception e) {
            throw new TripManagerException("Error creating trip", e);
        }
    }

    @Override
    public void addDateToTrip(int trip, Calendar date) throws TripManagerException {
        try (DBHelper h = new DBHelper(true)) {
            try (PreparedStatement stm = h.getConnection().prepareStatement("update " + trackTable + " set tripId=? where Date(TS)=?")) {
                stm.setInt(1, trip);
                stm.setTimestamp(2, new Timestamp(date.getTimeInMillis()));
                stm.executeUpdate();
            }
        } catch (Exception e) {
            throw new TripManagerException("Error adding days to trip", e);
        }
    }

    @Override
    public boolean setDescription(int trip, String description) throws TripManagerException {
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement st1 = db.getConnection().prepareStatement("update " + tripTable + " set description=? where id=?")) {
                st1.setString(1, description);
                st1.setInt(2, trip);
                int n = st1.executeUpdate();
                if (n == 1) {
                    synchronized (trips) {
                        trips.put(trip, description);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new TripManagerException("Error changing trip's description {" + trip + "} {" + description + "}", e);
        }
    }
}
