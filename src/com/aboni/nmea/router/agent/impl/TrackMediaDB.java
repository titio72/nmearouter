package com.aboni.nmea.router.agent.impl;

import java.sql.PreparedStatement;
import java.sql.Timestamp;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.agent.TrackMedia;
import com.aboni.utils.DBHelper;
import com.aboni.utils.ServerLog;

public class TrackMediaDB implements TrackMedia {

    private DBHelper db;
    private PreparedStatement stm;
    
    @Override
    public void writePoint(GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int interval) {
    	writePoint(p, anchor, dist, speed, maxSpeed, interval, 0);
    }

    public void writePoint(GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int interval, int count) {
    	boolean retry = false;
    	if (stm!=null) {
            try {
                stm.setDouble(1, p.getLatitude());
                stm.setDouble(2, p.getLongitude());
                Timestamp x = new Timestamp(p.getTimestamp());
                stm.setTimestamp(3, x);
                stm.setInt(4, anchor?1:0);
                stm.setInt(5, interval);
                stm.setDouble(6, speed);
                stm.setDouble(7, Math.max(maxSpeed, speed));
                stm.setDouble(8, dist);
                stm.execute();
            } catch (Exception e) {
            	retry = true;
                ServerLog.getLogger().Error("Cannot write down position (" + count + ")!", e);
            }
        }
    	if (retry) {
	    	count++;
	    	if (count<3) {
	    		resetConnection();
	    		writePoint(p, anchor, dist, speed, maxSpeed, interval, count);
	    	}
    	}
    }


    private void resetConnection() {
    	try {
    		if (db!=null) {
    			db.close();
    		}
    		init();
    	} catch (Exception e) {
            ServerLog.getLogger().Error("Error resetting connection!", e);
    	}
    }
    
    @Override
    public boolean init() {
        try {
            db = new DBHelper(true);
            stm = db.getConnection().prepareStatement("insert into track (lat, lon, TS, anchor, dTime, speed, maxSpeed, dist) values (?, ?, ?, ?, ?, ?, ?, ?)");
            return true;
        } catch (Exception e) {
            ServerLog.getLogger().Error("Cannot initialize track db!", e);
            return false;
        }
    }

    @Override
    public void dispose() {
        try {
            db.close();
            db = null;
            stm = null;
        } catch (Exception e) {}
    }

}
