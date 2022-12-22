/*
 * Copyright (c) 2022,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.data.track.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.data.track.TrackPoint;
import com.aboni.nmea.router.data.track.TrackPointBuilder;
import com.aboni.nmea.router.data.track.TrackScanner;
import com.aboni.nmea.router.data.track.TripManagerException;
import com.aboni.sensors.EngineStatus;
import com.aboni.utils.Log;
import com.aboni.utils.db.DBHelper;
import com.google.inject.Inject;

import javax.validation.constraints.NotNull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;

public class TrackScannerImpl implements TrackScanner {

    private final TrackPointBuilder builder;
    private final Log log;

    @Inject
    public TrackScannerImpl(Log log, @NotNull TrackPointBuilder builder) {
        this.builder = builder;
        this.log = log;
    }

    @Override
    public void scanTrip(Instant d0, Instant d1, TrackPointsScanner scanner) throws TripManagerException {
        try {
            load(scanner, d0, d1);
        } catch (Exception e) {
            throw new TripManagerException("Error loading track", e);
        }
    }

    private static TrackPoint getTrackPoint(TrackPointBuilder builder, ResultSet rs) throws SQLException {
        return builder.getNew().
                withPosition(new GeoPositionT(
                        rs.getTimestamp("TS").getTime(),
                        rs.getDouble("lat"),
                        rs.getDouble("lon"))).
                withPeriod(rs.getInt("dTime")).
                withSpeed(rs.getDouble("speed"), rs.getDouble("maxSpeed")).
                withDistance(rs.getDouble("dist")).
                withAnchor(rs.getInt("anchor") == 1).
                withEngine(EngineStatus.valueOf(rs.getInt("engine"))).getPoint();
    }

    private void load(TrackPointsScanner tripScanner, Instant d0, Instant d1) throws SQLException, MalformedConfigurationException, ClassNotFoundException {
        try (DBHelper db = new DBHelper(false)) {
            try (PreparedStatement st = db.getConnection().prepareStatement(
                    "select lat, lon, TS, id, speed, maxSpeed, anchor, dist, dTime, engine from track where TS>=? and TS<=?")) {
                st.setTimestamp(1, new Timestamp(d0.toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                st.setTimestamp(2, new Timestamp(d1.toEpochMilli()), Calendar.getInstance(TimeZone.getTimeZone("UTC")));
                if (st.execute()) {
                    try (ResultSet rs = st.getResultSet()) {
                        boolean goOn = true;
                        while (goOn && rs.next()) {
                            int id = rs.getInt("id");
                            try {
                                goOn = tripScanner.onTrackPoint(id, getTrackPoint(builder, rs));
                            } catch (Exception e) {
                                log.errorForceStacktrace("Error scanning trip", e);
                                goOn = false;
                            }
                        }
                    }
                }
            }
        }
    }
}
