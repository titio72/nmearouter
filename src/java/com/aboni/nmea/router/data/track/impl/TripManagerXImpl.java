package com.aboni.nmea.router.data.track.impl;

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.data.track.*;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBEventWriter;
import com.aboni.utils.db.DBHelper;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import java.sql.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TripManagerXImpl implements TripManagerX {

    public static final String ERROR_LOADING_TRIPS = "Error loading trips";
    private final TripArchive archive;
    private final String tripTable;
    private final String trackTable;
    private final DBEventWriter trackEventWriter;
    private final DBEventWriter tripEventWriter;

    @Inject
    public TripManagerXImpl(@Named(Constants.TAG_TRIP) String tripTableName,
                            @Named(Constants.TAG_TRACK) String trackTableName,
                            @Named(Constants.TAG_TRACK) DBEventWriter trackEventWriter,
                            @Named(Constants.TAG_TRIP) DBEventWriter tripEventWriter) {
        tripTable = tripTableName;
        trackTable = trackTableName;
        archive = new TripArchive();
        this.trackEventWriter = trackEventWriter;
        this.tripEventWriter = tripEventWriter;
        loadArchive(archive);
    }

    private void loadArchive(TripArchive archive) {
        try (ResultSet rs = new DBHelper(true).getConnection().createStatement().executeQuery("select id, description, fromTS, toTS, dist from " + tripTable)) {
            while (rs.next()) {
                TripImpl t = new TripImpl(rs.getInt("id"), rs.getString("description"));
                t.setTS(rs.getTimestamp("fromTS").toInstant());
                t.setTS(rs.getTimestamp("toTS").toInstant());
                t.setDistance(rs.getDouble("dist"));
                archive.setTrip(t);
            }
        } catch (ClassNotFoundException | SQLException e) {
            ServerLog.getLogger().error(ERROR_LOADING_TRIPS, e);
        }
    }

    private void saveTrip(Trip t, Connection conn) throws SQLException {
        String sql = "INSERT INTO " + tripTable + " (id, description, fromTS, toTS, dist) VALUES(?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE description=?, fromTS=?, toTS=?, dist=?";
        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, t.getTrip());
            stm.setString(2, t.getTripDescription());
            stm.setTimestamp(3, new Timestamp(t.getStartTS().toEpochMilli()));
            stm.setTimestamp(4, new Timestamp(t.getEndTS().toEpochMilli()));
            stm.setDouble(5, t.getDistance());
            stm.setString(6, t.getTripDescription());
            stm.setTimestamp(7, new Timestamp(t.getStartTS().toEpochMilli()));
            stm.setTimestamp(8, new Timestamp(t.getEndTS().toEpochMilli()));
            stm.setDouble(9, t.getDistance());
            stm.execute();
        }
    }

    public TripImpl getCurrentTrip(Instant now) {
        Trip t = archive.getLastTrip();
        if (t.getEndTS().plus(3, ChronoUnit.HOURS).isAfter(now) && t.getStartTS().minusMillis(1).isBefore(now)) {
            return (TripImpl) t;
        } else {
            return null;
        }
    }

    @Override
    public void onTrackPoint(TrackEvent event) throws TripManagerException {
        synchronized (archive) {
            TrackPoint point = event.getPoint();
            TripImpl t = getCurrentTrip(point.getPosition().getInstant());
            if (t == null) {
                t = (TripImpl) archive.getNew();
                t.setTS(point.getPosition().getInstant());
                t.addDistance(point.getDistance());
                try (DBHelper db = new DBHelper(false)) {
                    try (Connection conn = db.getConnection()) {
                        saveTrip(t, conn);
                        archive.setTrip(t);
                        trackEventWriter.write(event, conn);
                        conn.commit();
                    }
                } catch (ClassNotFoundException | SQLException e) {
                    throw new TripManagerException("Error starting a new trip from track point", e);
                }
            } else {
                try (DBHelper db = new DBHelper(false)) {
                    try (Connection conn = db.getConnection()) {
                        t.setTS(point.getPosition().getInstant());
                        t.addDistance(point.getDistance());
                        trackEventWriter.write(event, conn);
                        tripEventWriter.write(new TripEvent(t), conn);
                        conn.commit();
                    }
                } catch (ClassNotFoundException | SQLException e) {
                    throw new TripManagerException("Error writing rack point", e);
                }
            }
        }
    }

    @Override
    public Trip getTrip(Instant timestamp) {
        return archive.getTrip(timestamp);
    }

    @Override
    public Trip getTrip(int id) {
        return archive.getTrip(id);
    }

    @Override
    public void deleteTrip(int id) throws TripManagerException {
        Trip t = getTrip(id);
        if (t == null) throw new TripManagerException("Unknown trip " + id);
        else {
            archive.delete(id);
            try (DBHelper db = new DBHelper(false)) {
                try (PreparedStatement stm = db.getConnection().prepareStatement("delete from " + tripTable + " where id=?")) {
                    stm.setInt(1, id);
                    stm.execute();
                }
                try (PreparedStatement stm = db.getConnection().prepareStatement("delete from " + trackTable + " where TS>=? and TS<=?")) {
                    stm.setTimestamp(1, new Timestamp(t.getStartTS().toEpochMilli()));
                    stm.setTimestamp(2, new Timestamp(t.getEndTS().toEpochMilli()));
                    stm.execute();
                }
                db.getConnection().commit();
            } catch (ClassNotFoundException | SQLException e) {
                throw new TripManagerException("Error deleting trip " + id, e);
            }
        }
    }

    @Override
    public void setTripDescription(int id, @NotNull String description) throws TripManagerException {
        TripImpl t = (TripImpl) archive.getTrip(id);
        if (t == null) throw new TripManagerException("Unknown trip " + id);
        else {
            t.setTripDescription(description);
            try (Connection c = new DBHelper(true).getConnection()) {
                saveTrip(t, c);
            } catch (ClassNotFoundException | SQLException e) {
                throw new TripManagerException("Error saving trip", e);
            }
        }
    }

    @Override
    public List<Trip> getTrips(boolean desc) {
        synchronized (archive) {
            int sz = archive.tripsByDate.size();
            List<Trip> l = new ArrayList<>(sz);
            for (int i = 0; i < sz; i++) {
                l.add(archive.tripsByDate.get(desc ? (sz - i - 1) : i));
            }
            return Collections.unmodifiableList(l);
        }
    }

    @Override
    public List<Trip> getTrips(int year, boolean desc) {
        synchronized (archive) {
            int sz = archive.tripsByDate.size();
            List<Trip> l = new ArrayList<>(sz);
            for (int i = 0; i < sz; i++) {
                Trip t = archive.tripsByDate.get(desc ? (sz - i - 1) : i);
                if (t.getMaxDate().getYear() == year || t.getMinDate().getYear() == year) l.add(t);
            }
            return Collections.unmodifiableList(l);
        }
    }

    private static class TripArchive {
        final Map<Integer, Trip> tripsMap = new HashMap<>();
        final List<Trip> tripsByDate = new ArrayList<>();

        Trip getNew() {
            synchronized (this) {
                int newId = 0;
                for (int i : tripsMap.keySet()) {
                    newId = Math.max(i, newId);
                }
                newId++;
                return new TripImpl(newId, "Trip " + newId);
            }
        }

        void setTrip(Trip t) {
            synchronized (this) {
                Trip existingTrip = tripsMap.getOrDefault(t.getTrip(), null);
                if (existingTrip == null) {
                    tripsMap.put(t.getTrip(), t);
                } else {
                    int position = Collections.binarySearch(tripsByDate, existingTrip, Comparator.comparing(Trip::getStartTS));
                    tripsByDate.remove(position);
                }
                int insertAt = -Collections.binarySearch(tripsByDate, t, Comparator.comparing(Trip::getStartTS)) - 1;
                if (insertAt == tripsByDate.size()) tripsByDate.add(t);
                else tripsByDate.add(insertAt, t);

            }
        }

        Trip getTrip(int id) {
            return tripsMap.getOrDefault(id, null);
        }

        int getPosition(Instant timestamp) {
            synchronized (this) {
                TripImpl temp = new TripImpl(-99999999, "");
                temp.setTS(timestamp);
                int position = Collections.binarySearch(tripsByDate, temp, Comparator.comparing(Trip::getStartTS));
                if (position < 0) {
                    int p = -position - 1;
                    if (p > 0) {
                        Trip t = tripsByDate.get(p - 1);
                        if (t.getEndTS().compareTo(timestamp) >= 0) return p - 1;
                    }
                }
                return position;
            }
        }

        Trip getTrip(Instant timestamp) {
            synchronized (this) {
                int position = getPosition(timestamp);
                if (position >= 0) {
                    return tripsByDate.get(position);
                } else {
                    return null;
                }
            }
        }

        Trip getLastTrip() {
            synchronized (this) {
                if (tripsByDate.isEmpty()) return null;
                else return tripsByDate.get(tripsByDate.size() - 1);
            }
        }

        void delete(int id) {
            synchronized (this) {
                Trip t = getTrip(id);
                if (t != null) {
                    int position = getPosition(t.getStartTS());
                    tripsByDate.remove(position);
                    tripsMap.remove(id);
                }
            }
        }
    }
}
