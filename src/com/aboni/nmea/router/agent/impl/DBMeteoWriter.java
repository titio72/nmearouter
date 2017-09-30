package com.aboni.nmea.router.agent.impl;

import com.aboni.utils.Serie;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;
import com.aboni.utils.db.EventWriter;

public class DBMeteoWriter implements StatsWriter {
	
    private DBHelper db;
    private EventWriter ee;

    public DBMeteoWriter() {
    	ee = new DBMeteoEventWriter();
    }
    
    @Override
	public boolean init() {
    	if (db==null) {
            try {
				db = new DBHelper(true);
	        } catch (Exception e) {
	            ServerLog.getLogger().Error("Cannot initialize meteo stats writer!", e);
	            return false;
	        }
    	}
    	return true;
    }
    
    @Override
    public void write(Serie s, long ts) {
    	db.write(ee, new MeteoEvent(s, ts));
    }
    
    @Override
    public void dispose() {
    	if (db!=null) {
            db.close();
            db = null;
    	}
    }
}
