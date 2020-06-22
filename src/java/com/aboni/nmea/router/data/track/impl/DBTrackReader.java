/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.data.track.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.data.track.TrackManagementException;
import com.aboni.nmea.router.data.track.TrackPoint;
import com.aboni.nmea.router.data.track.TrackPointBuilder;
import com.aboni.nmea.router.data.track.TrackReader;
import com.aboni.sensors.EngineStatus;
import com.aboni.utils.*;
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

    private static final String SQL_BY_TRIP = "select TS, dist, speed, maxSpeed, engine, anchor, dTime, lat, lon from track " +
            "where TS>=(select fromTS from trip where id=?) and TS<=(select toTS from trip where id=?)";
    private static final String SQL_BY_DATE = "select TS, dist, speed, maxSpeed, engine, anchor, dTime, lat, lon from track where TS>=? and TS<?";

    public void readTrack(@NotNull Query query, @NotNull TrackReaderListener target) throws TrackManagementException {
        if (query instanceof QueryById) {
            readTrack(((QueryById) query).getId(), target);
        } else if (query instanceof QueryByDate) {
            readTrack(((QueryByDate) query).getFrom(), ((QueryByDate) query).getTo(), target);
        } else {
            throw new TrackManagementException("Unknown query " + query);
        }

    }

    private void readTrack(int tripId, @NotNull TrackReaderListener target) throws TrackManagementException {
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement st = db.getConnection().prepareStatement(SQL_BY_TRIP)) {
                st.setInt(1, tripId);
                st.setInt(2, tripId);
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
