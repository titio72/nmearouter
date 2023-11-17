package com.aboni.nmea.router.data.track.impl;

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.data.track.Trip;
import com.aboni.nmea.router.data.track.TripEvent;
import com.aboni.utils.Utils;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.*;

public class TripDAO {

    private final String tripTable;

    @Inject
    public TripDAO(@Named(Constants.TAG_TRIP) String tripTableName) {
        this.tripTable = tripTableName;
    }

    public interface TripCallback {
        void onTrip(Trip trip);
    }

    public void loadArchive(TripCallback callback, Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            try (ResultSet rs = s.executeQuery("select id, description, fromTS, toTS, dist, distSail, distMotor from " + tripTable)) {
                while (rs.next()) {
                    TripImpl t = new TripImpl(rs.getInt("id"), rs.getString("description"));
                    t.setTS(rs.getTimestamp("fromTS", Utils.UTC_CALENDAR).toInstant());
                    t.setTS(rs.getTimestamp("toTS", Utils.UTC_CALENDAR).toInstant());
                    t.setDistance(rs.getDouble("dist"));
                    t.setDistanceSail(rs.getDouble("distSail"));
                    t.setDistanceMotor(rs.getDouble("distMotor"));
                    callback.onTrip(t);
                }
            }
        }
    }

    public void deleteTrip(int id, Connection c) throws SQLException {
        try (PreparedStatement stm = c.prepareStatement("delete from " + tripTable + " where id=?")) {
            stm.setInt(1, id);
            stm.execute();
        }
    }

    public void saveTrip(Trip t, Connection conn) throws SQLException {
        String sql = "INSERT INTO " + tripTable + " (id, description, fromTS, toTS, dist, distSail, distMotor) VALUES(?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE description=?, fromTS=?, toTS=?, dist=?, distSail=?, distMotor=?";
        try (PreparedStatement stm = conn.prepareStatement(sql)) {
            stm.setInt(1, t.getTrip());
            stm.setString(2, t.getTripDescription());
            stm.setTimestamp(3, new Timestamp(t.getStartTS().toEpochMilli()), Utils.UTC_CALENDAR);
            stm.setTimestamp(4, new Timestamp(t.getEndTS().toEpochMilli()), Utils.UTC_CALENDAR);
            stm.setDouble(5, t.getDistance());
            stm.setDouble(6, t.getDistanceSail());
            stm.setDouble(7, t.getDistanceMotor());

            stm.setString(8, t.getTripDescription());
            stm.setTimestamp(9, new Timestamp(t.getStartTS().toEpochMilli()), Utils.UTC_CALENDAR);
            stm.setTimestamp(10, new Timestamp(t.getEndTS().toEpochMilli()), Utils.UTC_CALENDAR);
            stm.setDouble(11, t.getDistance());
            stm.setDouble(12, t.getDistanceSail());
            stm.setDouble(13, t.getDistanceMotor());
            stm.execute();
        }
    }

    public void updateTrip(TripEvent t, Connection c) throws SQLException {
        String sql = "UPDATE " + tripTable + " set toTS=?, dist=?, distSail=?, distMotor=? where id=?";
        try (PreparedStatement stm = c.prepareStatement(sql)) {
            stm.setTimestamp(1, new Timestamp(t.getTime()), Utils.UTC_CALENDAR);
            stm.setDouble(2, t.getTrip().getDistance());
            stm.setDouble(3, t.getTrip().getDistanceSail());
            stm.setDouble(4, t.getTrip().getDistanceMotor());
            stm.setInt(5, t.getTrip().getTrip());
            stm.execute();
        }
    }
}
