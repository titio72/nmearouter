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

import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.data.track.*;
import com.aboni.utils.Log;
import com.aboni.utils.db.DBHelper;
import com.google.inject.Inject;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TrackFixerService extends JSONWebService {

    private final TripManagerX tripManager;

    private static final int P_UPD_DTIME = 1;

    private static final int P_UPD_DISTANCE = 2;

    private static final int P_UPD_SPEED = 3;

    private static final int P_UPD_MAX_SPEED = 4;

    private static final int P_UPD_ID = 5;

    @Inject
    public TrackFixerService(Log log, @NotNull TrackPointBuilder pointBuilder, @NotNull TripManagerX tripManager, @NotNull TrackScanner scanner) {
        super(log);
        this.tripManager = tripManager;
        setLoader(config -> {
            int trackId = config.getInteger("track", 0);
            if (trackId != 0) {
                try {
                    TrackFixer fixer = load(pointBuilder, scanner, trackId);
                    tripManager.updateTripDistance(trackId, fixer.getTotDist());
                    JSONObject res = new JSONObject();
                    res.put("track", trackId);
                    res.put("changedPoints", fixer.getChangedPoints());
                    res.put("points", fixer.getPoints());
                    res.put("distance", fixer.getTotDist());
                    return res;
                } catch (Exception e) {
                    throw new JSONGenerationException("Error fixing track", e);
                }
            } else {
                throw new JSONGenerationException("No track selected");
            }
        });
    }

    private class TheFixer implements TrackScanner.TrackPointsScanner {

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
        public boolean onTrackPoint(int id, TrackPoint p) {
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
                    return false;
                }
            }
            return true;
        }
    }

    private TrackFixer load(TrackPointBuilder builder, TrackScanner scanner, int trackId) throws SQLException, MalformedConfigurationException, ClassNotFoundException, TripManagerException {
        TrackFixer res;
        Trip trip = tripManager.getTrip(trackId);
        if (trip != null) {
            try (DBHelper helper = new DBHelper(false)) {
                try (PreparedStatement stUpd = helper.getConnection().prepareStatement(
                        "update track set dTime=?, dist=?, speed=?, maxSpeed=? where id=?")) {
                    TheFixer theFixer = new TheFixer(helper, builder, stUpd);
                    scanner.scanTrip(trip.getStartTS(), trip.getEndTS(), theFixer);
                    res = theFixer.getFixer();
                    helper.getConnection().commit();
                }
            }
        } else {
            throw new TripManagerException("Trip " + trackId + " not found");
        }
        return res;
    }
}
