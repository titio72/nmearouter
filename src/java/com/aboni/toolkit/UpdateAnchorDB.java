package com.aboni.toolkit;

import com.aboni.geo.Course;
import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.agent.impl.track.TrackManager;
import com.aboni.nmea.router.agent.impl.track.TrackPoint;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class UpdateAnchorDB {

    private static class TrackItem {
        int id;
        GeoPositionT position;
        double speed;
        double maxSpeed;
        double distance;
        int deltaTime;
        boolean anchor;
    }

    private TrackManager trackManager = new TrackManager();

    private static TrackItem getItem(ResultSet rs) throws SQLException {
        // lat, lon, TS, anchor, id, speed, maxSpeed, dist, dTime

        TrackItem itm = new TrackItem();
        itm.id = rs.getInt(5);
        itm.position = new GeoPositionT(rs.getTimestamp(3).getTime(), rs.getDouble(1), rs.getDouble(2));
        itm.anchor = (rs.getInt(4) == 1);
        itm.speed = rs.getDouble(6);
        itm.maxSpeed = rs.getDouble(7);
        itm.distance = rs.getDouble(8);
        itm.deltaTime = rs.getInt(9);
        return itm;
    }

    private TrackItem last;

	public void load() {
        int idDiscrepancies = 0;
        try (DBHelper db = new DBHelper(false)) {
            try (PreparedStatement stUpd = db.getConnection().prepareStatement("update track set anchor=?, dist=?, dTime=?, speed=? where id=?")) {
                try (PreparedStatement st = db.getConnection().prepareStatement(
                        "select lat, lon, TS, anchor, id, speed, maxSpeed, dist, dTime from track order by id")) {

                    if (st.execute()) {
                        try (ResultSet rs = st.getResultSet()) {
                            int i = 0;
                            while (rs.next()) {
                                i++;
                                TrackItem item = getItem(rs);
                                TrackPoint tp = trackManager.processPosition(item.position, item.speed);

                                if (last != null) {
                                    if (last.position.getTimestamp() > item.position.getTimestamp()) {
                                        ServerLog.getConsoleOut().println("Discrepancy id: " + new Date(item.position.getTimestamp()) + " " + item.id);
                                        ServerLog.getConsoleOut().println("            id: " + new Date(last.position.getTimestamp()) + " " + last.id);
                                        ServerLog.getConsoleOut().println("            id: " + (item.position.getTimestamp() - last.position.getTimestamp()));
                                        idDiscrepancies++;
                                    } else {
                                        if ((item.position.getTimestamp() - last.position.getTimestamp()) < (6L * 60L * 60L * 1000L)) {
                                            Course c = new Course(last.position, item.position);
                                            double dist = c.getDistance();
                                            int interval = (int) (c.getInterval() / 1000);

                                            if ((tp == null || tp.isAnchor()) != item.anchor) {
                                                //ServerLog.getConsoleOut().println("Discrepancy: " + new Date(item.position.getTimestamp()));
                                            }
                                            stUpd.setInt(1, (tp == null || tp.isAnchor()) ? 1 : 0);
                                            stUpd.setDouble(2, dist);
                                            stUpd.setInt(3, interval);
                                            stUpd.setDouble(4, c.getSpeed());
                                            stUpd.setInt(5, item.id);
                                            stUpd.execute();
                                        } else {
                                            double speed = rs.getDouble(6);
                                            stUpd.setInt(1, speed > 0.1 ? 1 : 0);
                                            stUpd.setDouble(2, 0);
                                            stUpd.setInt(3, 0);
                                            stUpd.setDouble(4, speed);
                                            stUpd.setInt(5, item.id);
                                        }
                                    }
                                }
                                last = item;
                                if (i % 2500 == 0) {
                                    db.getConnection().commit();
                                    final int k = i;
                                    ServerLog.getConsoleOut().println("Done: " + k);
                                }
                            }
                            db.getConnection().commit();
                            final int count = i;
                            ServerLog.getConsoleOut().println("Processed %d points" + count);
                            ServerLog.getConsoleOut().println("wrong ids" + idDiscrepancies);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(ServerLog.getConsoleOut());
        }
	}

    public static void main(String[] args) {
		new UpdateAnchorDB().load();
	}

}
