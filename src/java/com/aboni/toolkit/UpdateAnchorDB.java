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

import com.aboni.geo.Course;
import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.data.track.TrackManager;
import com.aboni.nmea.router.data.track.TrackPoint;
import com.aboni.nmea.router.data.track.impl.TrackManagerImpl;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

public class UpdateAnchorDB {

    public static final long AGE_THRESHOLD = 6L * 60L * 60L * 1000L; //6h

    private static class TrackItem {
        int id;
        GeoPositionT position;
        double speed;
        boolean anchor;
    }

    private final TrackManager trackManager = new TrackManagerImpl(true);

    private static TrackItem getItem(ResultSet rs) throws SQLException {
        // lat, lon, TS, anchor, id, speed, maxSpeed, dist, dTime

        TrackItem itm = new TrackItem();
        itm.id = rs.getInt(5);
        itm.position = new GeoPositionT(rs.getTimestamp(3).getTime(), rs.getDouble(1), rs.getDouble(2));
        itm.anchor = (rs.getInt(4) == 1);
        itm.speed = rs.getDouble(6);
        return itm;
    }

    private TrackItem last;
    private int idDiscrepancies = 0;
    private int anchorDiscrepancies = 0;

    private boolean filter(TrackItem item) {
        Calendar d = Calendar.getInstance();
        d.setTimeInMillis(item.position.getTimestamp());
        return d.get(Calendar.YEAR) <= 2016;
    }

    public void load() {
        idDiscrepancies = 0;
        anchorDiscrepancies = 0;
        try (DBHelper db = new DBHelper(false)) {
            try (PreparedStatement stUpd = db.getConnection().prepareStatement("update track set anchor=?, dist=?, dTime=? where id=?")) {
                try (PreparedStatement st = db.getConnection().prepareStatement(
                        "select lat, lon, TS, anchor, id, speed, maxSpeed, dist, dTime from track order by id")) {

                    if (st.execute()) {
                        try (ResultSet rs = st.getResultSet()) {
                            int i = 0;
                            while (rs.next()) {
                                i++;
                                TrackItem item = getItem(rs);
                                TrackPoint tp = trackManager.processPosition(item.position, item.speed);

                                processPoint(item, tp, stUpd);

                                last = item;
                                if (i % 2500 == 0) {
                                    db.getConnection().commit();
                                }
                            }
                            db.getConnection().commit();
                            final int count = i;
                            ServerLog.getLogger().console("Processed %d points " + count);
                            ServerLog.getLogger().console("wrong ids   " + idDiscrepancies);
                            ServerLog.getLogger().console("anchor      " + anchorDiscrepancies);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isLastValid(TrackItem item) {
        return (last != null && last.position.getTimestamp() > item.position.getTimestamp());
    }

    private boolean isRecent(TrackItem item) {
        return (last != null && (item.position.getTimestamp() - last.position.getTimestamp()) < AGE_THRESHOLD);
    }

    private void processPoint(TrackItem item, TrackPoint tp, PreparedStatement stUpd) throws SQLException {
        if (filter(item)) {
            if (isLastValid(item)) {
                // very bad issue... consecutive points are not ordered with time
                ServerLog.getLogger().console("Discrepancy id: " + new Date(item.position.getTimestamp()) + " " + item.id);
                ServerLog.getLogger().console("            id: " + new Date(last.position.getTimestamp()) + " " + last.id);
                ServerLog.getLogger().console("            id: " + (item.position.getTimestamp() - last.position.getTimestamp()));
                idDiscrepancies++;
            } else if (isRecent(item)) {
                updateSameLeg(item, tp, stUpd);
            } else {
                startNewLeg(item, stUpd);
            }
        }
    }

    private void startNewLeg(TrackItem item, PreparedStatement stUpd) throws SQLException {
        if ((item.speed < 1.0) != item.anchor) {
            logItem(item, true);
        }
        stUpd.setInt(1, item.speed < 0.1 ? 1 : 0);
        stUpd.setDouble(2, 0);
        stUpd.setInt(3, 0);
        stUpd.setInt(4, item.id);
        stUpd.execute();
    }

    private void updateSameLeg(TrackItem item, TrackPoint tp, PreparedStatement stUpd) throws SQLException {
        Course c = new Course(last.position, item.position);
        double dist = c.getDistance();
        int interval = (int) (c.getInterval() / 1000);

        boolean anchor;
        if (tp == null) {
            // track manager says that this sample should not be tracked (so you're moored)
            // speed check should not be necessary but...
            anchor = item.speed < 1.0;
        } else {
            anchor = tp.isAnchor();
        }

        if (anchor != item.anchor) {
            logItem(item, anchor);
            anchorDiscrepancies++;
        }
        stUpd.setInt(1, anchor ? 1 : 0);
        stUpd.setDouble(2, dist);
        stUpd.setInt(3, interval);
        stUpd.setInt(4, item.id);
        stUpd.execute();
    }


    private void logItem(TrackItem item, boolean expectedAnchor) {
        ServerLog.getLogger().console(String.format("Discrepancy: %s %s %d %s Speed %f.2 %n",
                item.anchor ? "Y" : "N", expectedAnchor ? "Y" : "N", item.id,
                new Date(item.position.getTimestamp()), item.speed));
    }

    public static void main(String[] args) {
        new UpdateAnchorDB().load();
    }

}
