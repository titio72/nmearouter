package com.aboni.nmea.router.data.track.impl;

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.data.track.TrackEvent;
import com.aboni.data.Pair;
import com.aboni.utils.Utils;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.*;
import java.time.Instant;

public class TrackDAO {

    private final String trackTable;

    @Inject
    public TrackDAO(@Named(Constants.TAG_TRACK) String trackTableName) {
        trackTable = trackTableName;
    }

    public void deleteFromTrack(Instant t0, Instant t1, Connection connection, boolean includeStart) throws SQLException {
        try (PreparedStatement stm = connection.prepareStatement("delete from " + trackTable + " where TS" + (includeStart ? ">=" : ">") + "? and TS" + (includeStart ? "<=" : "<") + "?")) {
            stm.setTimestamp(1, new Timestamp(t0.toEpochMilli()), Utils.UTC_CALENDAR);
            stm.setTimestamp(2, new Timestamp(t1.toEpochMilli()), Utils.UTC_CALENDAR);
            stm.execute();
        }
    }

    public Pair<Instant, Instant> getTrimmedTrip(Instant startTime, Instant stopTime, Connection c) throws SQLException {
        try (PreparedStatement stm = c.prepareStatement("select min(TS), max(TS) from " + trackTable + " where TS>=? and TS<=? and anchor=0")) {
            stm.setTimestamp(1, new Timestamp(startTime.toEpochMilli()), Utils.UTC_CALENDAR);
            stm.setTimestamp(2, new Timestamp(stopTime.toEpochMilli()), Utils.UTC_CALENDAR);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                return new Pair<>(rs.getTimestamp(1, Utils.UTC_CALENDAR).toInstant(),
                        rs.getTimestamp(2, Utils.UTC_CALENDAR).toInstant());
            } else {
                return null;
            }
        }
    }

    public void writeEvent(TrackEvent t, Connection conn) throws SQLException{
        try (PreparedStatement stm = conn.prepareStatement("insert into " + trackTable + " (lat, lon, TS, anchor, dTime, speed, maxSpeed, dist, engine) values (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            stm.setDouble(1, t.getPoint().getPosition().getLatitude());
            stm.setDouble(2, t.getPoint().getPosition().getLongitude());
            Timestamp x = new Timestamp(t.getTime());
            stm.setTimestamp(3, x, Utils.UTC_CALENDAR);
            stm.setInt(4, t.getPoint().isAnchor() ? 1 : 0);
            stm.setInt(5, t.getPoint().getPeriod());
            stm.setDouble(6, t.getPoint().getAverageSpeed());
            stm.setDouble(7, Math.max(t.getPoint().getMaxSpeed(), t.getPoint().getAverageSpeed()));
            stm.setDouble(8, t.getPoint().getDistance());
            stm.setByte(9, t.getPoint().getEngine().toByte());
            stm.execute();
        }
    }
}
