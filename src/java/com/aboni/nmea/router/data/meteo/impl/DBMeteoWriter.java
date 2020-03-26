package com.aboni.nmea.router.data.meteo.impl;

import com.aboni.utils.ServerLog;
import com.aboni.utils.StatsSample;
import com.aboni.utils.StatsWriter;
import com.aboni.utils.db.DBEventWriter;
import com.aboni.utils.db.DBHelper;

public class DBMeteoWriter implements StatsWriter {

    private DBHelper db;
    private final DBEventWriter ee;

    public DBMeteoWriter() {
    	ee = new DBMeteoEventWriter();
    }
    
    @Override
	public void init() {
    	if (db==null) {
            try {
				db = new DBHelper(true);
	        } catch (Exception e) {
	            ServerLog.getLogger().error("Cannot initialize meteo stats writer!", e);
            }
    	}
    }
    
    @Override
    public void write(StatsSample s, long ts) {
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
