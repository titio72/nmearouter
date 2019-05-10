package com.aboni.toolkit;

import com.aboni.geo.Course;
import com.aboni.geo.GeoPositionT;
import com.aboni.utils.db.DBHelper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateAnchorDB {

	private GeoPositionT last;
	
	public void load() {
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement stUpd = db.getConnection().prepareStatement("update track set anchor=1 where id=?")) {
                try (PreparedStatement st = db.getConnection().prepareStatement(
                        "select lat, lon, TS, anchor, id from track order by TS")) {

                    if (st.execute()) {
                        try (ResultSet rs = st.getResultSet()) {
                            int i = 0;
                            while (rs.next()) {
                                i++;
                                double lat = rs.getDouble(1);
                                double lon = rs.getDouble(2);
                                Timestamp ts = rs.getTimestamp(3);
                                boolean anchor = (rs.getInt(4) == 1);
                                int id = rs.getInt(5);
                                GeoPositionT p = new GeoPositionT(ts.getTime(), lat, lon);
                                if (last != null) {
                                    Course c = new Course(last, p);
                                    double dist = c.getDistance();
                                    long interval = c.getInterval();
                                    if (checkAnchor(dist, interval) /* 40m */ && !anchor) {
                                        stUpd.setInt(1, id);
                                        stUpd.execute();
                                    }
                                }
                                last = p;
                            }
                            final int count = i;
                            Logger.getGlobal().info(() -> String.format( "Processed %d points", count));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Error seting anchor flag", e);
        }
	}

    private boolean checkAnchor(double dist, long interval) {
        return interval <= (60 * 60 * 1000) && interval > (70 * 1000)/*1h*/ && dist < 0.02;
    }

    public static void main(String[] args) {
		Logger.getGlobal().info("start");
		new UpdateAnchorDB().load();
        Logger.getGlobal().info("end");
	}

}
