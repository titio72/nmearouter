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
import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.data.Query;
import com.aboni.nmea.router.data.QueryByDate;
import com.aboni.nmea.router.data.QueryById;
import com.aboni.nmea.router.data.track.TrackManagementException;
import com.aboni.nmea.router.data.track.TrackPoint;
import com.aboni.nmea.router.data.track.TrackPointBuilder;
import com.aboni.nmea.router.data.track.TrackReader;
import com.aboni.log.Log;
import com.aboni.log.SafeLog;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.sensors.EngineStatus;
import com.aboni.utils.Utils;
import net.sf.marineapi.nmea.util.Position;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class DBTrackReader implements TrackReader {

    private static final String ERROR_READING_TRACK = "Error reading track";
    public static final String TRACK_READER_LISTENER_IS_NULL = "Track reader listener is null";
    private final Log log;

    @Inject
    public DBTrackReader(Log log, @Named(Constants.TAG_TRACK) String tableName) {
        this.log = SafeLog.getSafeLog(log);
        sqlByTrip = "select TS, dist, speed, maxSpeed, engine, anchor, dTime, lat, lon, id from " + tableName +
            " where TS>=(select fromTS from trip where id=?) and TS<=(select toTS from trip where id=?)";
        sqlByDate = "select TS, dist, speed, maxSpeed, engine, anchor, dTime, lat, lon, id from " + tableName + " where TS>=? and TS<=?";
    }

    private final String sqlByTrip;
    private final String sqlByDate;

    public void readTrack(Query query, TrackReaderListener target) throws TrackManagementException {
        if (target==null) throw new TrackManagementException(TRACK_READER_LISTENER_IS_NULL);
        if (query instanceof QueryById) {
            readTrack(((QueryById) query).getId(), target);
        } else if (query instanceof QueryByDate) {
            readTrack(((QueryByDate) query).getFrom(), ((QueryByDate) query).getTo(), target);
        } else {
            throw new TrackManagementException("Unknown query " + query);
        }
    }

    private void readTrack(int tripId, TrackReaderListener target) throws TrackManagementException {
        if (tripId<0) throw new IllegalArgumentException("Invalid trip id");
        readTrack(sqlByTrip, (PreparedStatement st)->{
            st.setInt(1, tripId);
            st.setInt(2, tripId);
            }, target);
    }

    private void readTrack(Instant from, Instant to, TrackReaderListener target) throws TrackManagementException {
        if (from==null || to==null || from.isAfter(to)) throw new TrackManagementException("Invalid query dates");
        readTrack(sqlByDate, (PreparedStatement st)->{
            st.setTimestamp(1, new Timestamp(from.toEpochMilli()), Utils.UTC_CALENDAR);
            st.setTimestamp(2, new Timestamp(to.toEpochMilli()), Utils.UTC_CALENDAR);
            }, target);
    }

    private void readTrack(String sql, DBHelper.QueryPreparer preparer, TrackReaderListener target) throws TrackManagementException {
        if (target==null) throw new TrackManagementException(TRACK_READER_LISTENER_IS_NULL);
        try (DBHelper db = new DBHelper(log, true)) {
            db.executeQuery(sql, preparer, (ResultSet rs)->{
                while (rs.next()) {
                    target.onRead(rs.getInt(10), getSample(rs));
                }
            });
        } catch (ClassNotFoundException | MalformedConfigurationException | SQLException e) {
            throw new TrackManagementException(ERROR_READING_TRACK, e);
        }
    }

    private TrackPoint getSample(ResultSet rs) throws SQLException {
        TrackPointBuilder builder = ThingsFactory.getInstance(TrackPointBuilder.class);
        return builder
                .withPosition(new GeoPositionT(rs.getTimestamp("TS", Utils.UTC_CALENDAR).getTime(), new Position(rs.getDouble("lat"), rs.getDouble("lon"))))
                .withAnchor(1 == rs.getInt("anchor"))
                .withDistance(rs.getDouble("dist"))
                .withSpeed(rs.getDouble("speed"), rs.getDouble("maxSpeed"))
                .withPeriod(rs.getInt("dTime"))
                .withEngine(EngineStatus.valueOf(rs.getInt("engine"))).getPoint();
    }
}
