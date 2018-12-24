package com.aboni.nmea.router.batch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import com.aboni.geo.Course;
import com.aboni.geo.GeoPositionT;
import com.aboni.utils.db.DBHelper;

public class UpdateAnchorDB {

	public UpdateAnchorDB() {
	}
	
	private GeoPositionT last;
	
	public void load() {
        try (DBHelper db = new DBHelper(true)) {
            PreparedStatement stUpd = db.getConnection().prepareStatement("update track set anchor=1 where id=?");
            PreparedStatement st = db.getConnection().prepareStatement(
                    "select lat, lon, TS, anchor, id from track order by TS");

            if (st.execute()) {
                ResultSet rs = st.getResultSet();
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
                        if (interval <= (60 * 60 * 1000) && interval > (70 * 1000)/*1h*/ && dist < 0.02 /* 40m */) {
                            if (!anchor) {
                                stUpd.setInt(1, id);
                                stUpd.execute();
                            }
                        }
                    }
                    last = p;
                }
                System.out.println(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static void main(String[] args) {
		System.out.println("start");
		new UpdateAnchorDB().load();
		System.out.println("end");
	}

}
