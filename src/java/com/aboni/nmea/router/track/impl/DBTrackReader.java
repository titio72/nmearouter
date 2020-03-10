package com.aboni.nmea.router.track.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.track.*;
import com.aboni.sensors.EngineStatus;
import com.aboni.utils.ServerLog;
import com.aboni.utils.ThingsFactory;
import com.aboni.utils.db.DBHelper;
import net.sf.marineapi.nmea.util.Position;

import javax.validation.constraints.NotNull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class DBTrackReader implements TrackReader {

    private static final String ERROR_READING_TRACK = "Error reading track";

    public DBTrackReader() {
        // nothing to initialize
    }

    private static final String SQL_BY_TRIP = "select TS, dist, speed, maxSpeed, engine, anchor, dTime, lat, lon from track where tripid=?";
    private static final String SQL_BY_DATE = "select TS, dist, speed, maxSpeed, engine, anchor, dTime, lat, lon from track where TS>=? and TS<?";

    public void readTrack(@NotNull TrackQuery query, @NotNull TrackReaderListener target) throws TrackManagementException {
        if (query instanceof TrackQueryById) {
            readTrack(((TrackQueryById) query).getTrackId(), target);
        } else if (query instanceof TrackQueryByDate) {
            readTrack(((TrackQueryByDate) query).getFrom(), ((TrackQueryByDate) query).getTo(), target);
        } else {
            throw new TrackManagementException("Unknown query " + query);
        }

    }

    private void readTrack(int tripId, @NotNull TrackReaderListener target) throws TrackManagementException {
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement st = db.getConnection().prepareStatement(SQL_BY_TRIP)) {
                st.setInt(1, tripId);
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        target.onRead(getSample(rs));
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            ServerLog.getLogger().error(ERROR_READING_TRACK, e);
            throw new TrackManagementException(ERROR_READING_TRACK, e);
        }
    }

    private void readTrack(@NotNull Instant from, @NotNull Instant to, @NotNull TrackReaderListener target) throws TrackManagementException {
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement st = db.getConnection().prepareStatement(SQL_BY_DATE)) {
                st.setTimestamp(1, new Timestamp(from.toEpochMilli()));
                st.setTimestamp(2, new Timestamp(to.toEpochMilli()));
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        target.onRead(getSample(rs));
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            ServerLog.getLogger().error(ERROR_READING_TRACK, e);
            throw new TrackManagementException(ERROR_READING_TRACK, e);
        }
    }

    private TrackPoint getSample(ResultSet rs) throws SQLException {
        TrackPointBuilder builder = ThingsFactory.getInstance(TrackPointBuilder.class);
        return builder
                .withPosition(new GeoPositionT(rs.getTimestamp("TS").getTime(), new Position(rs.getDouble("lat"), rs.getDouble("lon"))))
                .withAnchor(1 == rs.getInt("anchor"))
                .withDistance(rs.getDouble("dist"))
                .withSpeed(rs.getDouble("speed"), rs.getDouble("maxSpeed"))
                .withPeriod(rs.getInt("dTime"))
                .withEngine(EngineStatus.valueOf(rs.getInt("engine"))).getPoint();
    }
}
