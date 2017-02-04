package com.aboni.nmea.router.batch;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import com.aboni.geo.Course;
import com.aboni.geo.GeoPositionT;
import com.aboni.utils.DBHelper;

public class UpdateDistanceAndSpeed {

	public UpdateDistanceAndSpeed() {
	}
	
	private GeoPositionT last = null;
	
	public void load() {
	    DBHelper db = null;
	    try {
	        db = new DBHelper(false);
	        PreparedStatement stUpd = db.getConnection().prepareStatement(
	        		"update track set dTime=?, dist=?, speed=? where id=?");
    	    PreparedStatement st = db.getConnection().prepareStatement(
    	            "select lat, lon, TS, id from track where id>156694 order by TS");
    	    
    	    if (st.execute()) {
    	        ResultSet rs = st.getResultSet();
    	        int i = 0;
    	        while (rs.next()) {
                    double lat = rs.getDouble(1);
                    double lon = rs.getDouble(2);
                    Timestamp ts = rs.getTimestamp(3);
                    int id = rs.getInt(4);
                    long t = ts.getTime();
    	            GeoPositionT p = new GeoPositionT(t, lat, lon);
    	            if (last!=null) {
    	            	Course c = new Course(last, p);
    	            	double interval = (double)(c.getInterval()) / (1000.0*60.0*60.0); // interval in hours
    	            	if (interval < 5 /* less than 5 hours */) {
	    	            	double dist = c.getDistance();
	    	            	double speed = dist / interval;
	    	            	stUpd.setInt(1, (int) (c.getInterval()/1000));
	    	            	stUpd.setDouble(2, dist);
	    	            	if (Double.isNaN(speed))
		    	            	stUpd.setDouble(3, 0.0);
	    	            	else
	    	            		stUpd.setDouble(3, speed);
	    	            	stUpd.setInt(4, id);
	    	            	i += stUpd.executeUpdate();
	    	            	if (i%1000==0) {
	    	            		System.out.print("#");
	    	            		db.getConnection().commit();
	    	            	}
    	            	}
    	            }
    	            last = p;
    	        }
        		db.getConnection().commit();
    	        System.out.println(i);
    	    }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
                db.close();
            } catch (Exception e) { }
	    }
	}
	
	public static void main(String[] args) {
		System.out.println("start");
		new UpdateDistanceAndSpeed().load();
		System.out.println("end");
	}

}
