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

package com.aboni.nmea.router.services;

import com.aboni.log.Log;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.data.Query;
import com.aboni.nmea.router.data.QueryByDate;
import com.aboni.nmea.router.data.track.*;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.google.inject.Inject;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TrackFixerService extends JSONWebService {

    public static final String SQL_UPDATE_TRACK = "update track set dTime=?, dist=?, speed=?, maxSpeed=? where id=?";
    private final TripManagerX tripManager;

    private static final int P_UPD_DTIME = 1;

    private static final int P_UPD_DISTANCE = 2;

    private static final int P_UPD_SPEED = 3;

    private static final int P_UPD_MAX_SPEED = 4;

    private static final int P_UPD_ID = 5;

    @Inject
    public TrackFixerService(Log log, TrackPointBuilder pointBuilder, TripManagerX tripManager, TrackReader scanner) {
        super(log);
        if (tripManager==null) throw new IllegalArgumentException("Trip manager is null");
        if (pointBuilder==null) throw new IllegalArgumentException("Track point builder is null");
        if (scanner==null) throw new IllegalArgumentException("Track reader is null");
        this.tripManager = tripManager;
        setLoader((ServiceConfig config) -> {
            int trackId = config.getInteger("trip", 0);
            if (trackId != 0) {
                try {
                    TrackFixer fixer = load(pointBuilder, scanner, trackId);
                    tripManager.updateTripDistance(trackId, fixer.getTotDist());
                    JSONObject res = new JSONObject();
                    res.put("trip", trackId);
                    res.put("changedPoints", fixer.getChangedPoints());
                    res.put("points", fixer.getPoints());
                    res.put("distance", fixer.getTotDist());
                    return res;
                } catch (Exception e) {
                    throw new JSONGenerationException("Error fixing track {" + e.getMessage() + "}", e);
                }
            } else {
                throw new JSONGenerationException("No track selected");
            }
        });
    }

    private class TheFixer implements TrackReader.TrackReaderListener {

        private final TrackFixer fixer = new TrackFixer();
        private final TrackPointBuilder builder;
        private final PreparedStatement stUpd;
        private final DBHelper helper;
        private int i = 0;

        TheFixer(DBHelper helper, TrackPointBuilder builder, PreparedStatement stUpd) {
            this.helper = helper;
            this.builder = builder;
            this.stUpd = stUpd;
        }

        TrackFixer getFixer() {
            return fixer;
        }

        @Override
        public void onRead(int id, TrackPoint p) throws TrackManagementException {
            TrackPoint point = fixer.onTrackPoint(builder, p);
            if (point != null) {
                i++;
                try {
                    stUpd.setInt(P_UPD_DTIME, point.getPeriod());
                    stUpd.setDouble(P_UPD_DISTANCE, point.getDistance());
                    stUpd.setDouble(P_UPD_SPEED, point.getAverageSpeed());
                    stUpd.setDouble(P_UPD_MAX_SPEED, point.getMaxSpeed());
                    stUpd.setInt(P_UPD_ID, id);
                    i += stUpd.executeUpdate();
                    if (i % 1000 == 0) {
                        // commit every 1000 updates
                        helper.getConnection().commit();
                    }
                } catch (SQLException e) {
                    getLogger().error("Error fixing track", e);
                    throw new TrackManagementException(e);
                }
            }
        }
    }

    private TrackFixer load(TrackPointBuilder builder, TrackReader scanner, int trackId) throws SQLException, MalformedConfigurationException, TrackManagementException {
        TrackFixer res;
        Trip trip = tripManager.getTrip(trackId);
        if (trip != null) {
            try (
                    DBHelper helper = new DBHelper(getLogger(), false);
                    PreparedStatement stUpd = helper.getConnection().prepareStatement(SQL_UPDATE_TRACK)) {
                TheFixer theFixer = new TheFixer(helper, builder, stUpd);
                Query q = new QueryByDate(trip.getStartTS(), trip.getEndTS());
                scanner.readTrack(q, theFixer);
                res = theFixer.getFixer();
                helper.getConnection().commit();
            }
        } else {
            throw new TripManagerException("Trip " + trackId + " not found");
        }
        return res;
    }
}
