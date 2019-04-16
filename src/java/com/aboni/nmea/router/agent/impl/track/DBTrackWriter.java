package com.aboni.nmea.router.agent.impl.track;

import com.aboni.geo.GeoPositionT;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;
import com.aboni.utils.db.EventWriter;

public class DBTrackWriter implements TrackWriter {

    private DBHelper db;
    private final EventWriter primary;
    private final FileTrackWriter fallbackWriter;
    
    public DBTrackWriter() {
    	primary = new DBTrackEventWriter();
    	fallbackWriter = new FileTrackWriter("track.err");
    }
    
    @Override
    public void write(GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int interval) {
    	if (!db.write(primary, new TrackEvent(p, anchor, dist, speed, maxSpeed, interval))) {
    		fallbackWriter.write(p, anchor, dist, speed, maxSpeed, interval);
    	}
    }
    
    @Override
    public boolean init() {
        try {
            db = new DBHelper(true);
            return true;
        } catch (Exception e) {
            ServerLog.getLogger().Error("Cannot initialize track db!", e);
            return false;
        }
    }

    @Override
    public void dispose() {
        db.close();
        db = null;
    }
}
