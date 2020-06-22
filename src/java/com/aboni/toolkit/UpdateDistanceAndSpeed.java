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
import com.aboni.utils.db.DBHelper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateDistanceAndSpeed {

    private GeoPositionT last = null;

    public void load() {
        try (DBHelper db = new DBHelper(false)) {
            try (PreparedStatement st = db.getConnection().prepareStatement(
                    "select lat, lon, TS, id from track where TS>'2020-06-20' and TS<'2020-06-22'")) {

                if (st.execute()) {
                    try (ResultSet rs = st.getResultSet()) {
                        scanAndUpdate(db, rs);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Error", e);
        }
    }

    private void scanAndUpdate(DBHelper db, ResultSet rs) throws SQLException {
        try (PreparedStatement stUpd = db.getConnection().prepareStatement(
                "update track set dTime=?, dist=?, speed=? where id=?")) {
            int i = 0;
            while (rs.next()) {
                double lat = rs.getDouble(1);
                double lon = rs.getDouble(2);
                Timestamp ts = rs.getTimestamp(3);
                int id = rs.getInt(4);
                long t = ts.getTime();
                GeoPositionT p = new GeoPositionT(t, lat, lon);
                if (last != null) {
                    Course c = new Course(last, p);
                    double interval = (double) (c.getInterval()) / (1000.0 * 60.0 * 60.0); // interval in hours
                    if (interval < 5 /* less than 5 hours */) {
                        double dist = c.getDistance();
                        double speed = dist / interval;
                        stUpd.setInt(1, (int) (c.getInterval() / 1000));
                        stUpd.setDouble(2, dist);
                        if (Double.isNaN(speed))
                            stUpd.setDouble(3, 0.0);
                        else
                            stUpd.setDouble(3, speed);
                        stUpd.setInt(4, id);
                        i += stUpd.executeUpdate();
                        if (i % 1000 == 0) {
                            db.getConnection().commit();
                        }
                    }
                }
                last = p;
            }
            db.getConnection().commit();
        }
    }

    public static void main(String[] args) {
        new UpdateDistanceAndSpeed().load();
    }
}
