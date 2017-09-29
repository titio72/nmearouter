package com.aboni.nmea.router.agent.impl;

import java.sql.PreparedStatement;
import java.sql.Timestamp;

import com.aboni.utils.DBHelper;
import com.aboni.utils.Serie;
import com.aboni.utils.ServerLog;

public class DBStatsWriter implements StatsWriter {
	
    private DBHelper db;
    private PreparedStatement stm;

    public DBStatsWriter() {
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
	public void init() {
    	if (db==null) {
            try {
				db = new DBHelper(true);
	            stm = db.getConnection().prepareStatement("insert into meteo (type, v, vMax, vMin, TS) values (?, ?, ?, ?, ?)");
	        } catch (Exception e) {
	            ServerLog.getLogger().Error("Cannot initialize meteo stats writer!", e);
	        }
    	}
    }
    
    @Override
    public void write(Serie s, long ts) {
    	write(s, ts, 0);
    }
    
    public void write(Serie s, long ts, int count) {
    	boolean retry = false;
    	if (stm!=null) {
            try {
				stm.setString(1, s.getTag());
	            stm.setDouble(2, s.getAvg());
	            stm.setDouble(3, s.getMax());
	            stm.setDouble(4, s.getMin());
	            stm.setTimestamp(5, new Timestamp(ts));
	            stm.execute();
	        } catch (Exception e) {
	        	ServerLog.getLogger().Error("Cannot write meteo info type {" + s.getType() + "} Value {" + s.getAvg() +"} (" + count + ")", e);
	        	retry = true;
	        }
    	}
    	if (retry) {
	    	count++;
	    	if (count<3) {
	    		resetConnection();
	    		write(s, ts, count);
	    	}
    	}    	
    }
    
    @Override
    public void dispose() {
    	if (db!=null) {
            try {
                db.close();
            } catch (Exception e) {}
            db = null;
            stm = null;
    	}
    }
}
