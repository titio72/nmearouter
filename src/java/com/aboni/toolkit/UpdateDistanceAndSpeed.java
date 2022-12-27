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

package com.aboni.toolkit;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.conf.ConfJSON;
import com.aboni.nmea.router.data.track.TrackFixer;
import com.aboni.nmea.router.data.track.TrackPoint;
import com.aboni.nmea.router.data.track.TrackPointBuilder;
import com.aboni.nmea.router.utils.LogAdmin;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.sensors.EngineStatus;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateDistanceAndSpeed {

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

    public void load(TrackPointBuilder builder) {
        try (DBHelper db = new DBHelper(false)) {
/*            try (PreparedStatement st = db.getConnection().prepareStatement(
                    "select lat, lon, TS, id, speed, maxSpeed, anchor, dTime, engine from track where " +
                            "TS>=(select fromTS from trip where id=215) and" +
                            "TS<=(select toTS from trip where id=215)")) {*/
            try (PreparedStatement st = db.getConnection().prepareStatement(
                    "select lat, lon, TS, id, speed, maxSpeed, anchor, dTime, engine from track where " +
                            "TS>'2022-12-19'")) {

                if (st.execute()) {
                    try (ResultSet rs = st.getResultSet()) {
                        scanAndUpdate(builder, db, rs);
                    }
                }
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Error", e);
        }
    }

    private static final int P_UPD_DTIME = 1;
    private static final int P_UPD_DISTANCE = 2;
    private static final int P_UPD_SPEED = 3;
    private static final int P_UPD_MAX_SPEED = 4;
    private static final int P_UPD_ID = 5;


    private void scanAndUpdate(TrackPointBuilder builder, DBHelper db, ResultSet rs) throws SQLException {
        try (PreparedStatement stUpd = db.getConnection().prepareStatement(
                "update track set dTime=?, dist=?, speed=?, maxSpeed=? where id=?")) {
            int i = 0;
            TrackFixer fixer = new TrackFixer();
            while (rs.next()) {
                int id = rs.getInt("id");
                TrackPoint pointOrig = getTrackPoint(builder, rs);
                TrackPoint point = fixer.onTrackPoint(builder, pointOrig);
                if (point != null) {
                    i++;
                    stUpd.setInt(P_UPD_DTIME, point.getPeriod());
                    stUpd.setDouble(P_UPD_DISTANCE, point.getDistance());
                    stUpd.setDouble(P_UPD_SPEED, point.getAverageSpeed());
                    stUpd.setDouble(P_UPD_MAX_SPEED, point.getMaxSpeed());
                    stUpd.setInt(P_UPD_ID, id);
                    i += stUpd.executeUpdate();
                    if (i % 1000 == 0) {
                        // commit every 1000 updates
                        db.getConnection().commit();
                    }
                }
            }
            db.getConnection().commit();
            System.out.printf("Dist %5.2f Upd %d over %d points\n", fixer.getTotDist(), fixer.getChangedPoints(), fixer.getPoints());
        }
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new NMEARouterModule());
        ThingsFactory.setInjector(injector);
        ThingsFactory.getInstance(LogAdmin.class);
        ConfJSON cJ;
        try {
            cJ = new ConfJSON();
            cJ.getLogLevel();
        } catch (Exception e) {
            e.printStackTrace();
        }

        new UpdateDistanceAndSpeed().load(ThingsFactory.getInstance(TrackPointBuilder.class));
    }
}
