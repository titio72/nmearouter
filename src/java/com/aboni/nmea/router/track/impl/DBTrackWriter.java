package com.aboni.nmea.router.track.impl;

import com.aboni.nmea.router.track.TrackEvent;
import com.aboni.nmea.router.track.TrackPoint;
import com.aboni.nmea.router.track.TrackWriter;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;
import com.aboni.utils.db.EventWriter;

import javax.inject.Inject;
import javax.inject.Named;

public class DBTrackWriter implements TrackWriter {

    private DBHelper db;
    private final EventWriter primary;
    private final FileTrackWriter fallbackWriter;

    @Inject
    public DBTrackWriter(@Named("TrackTableName") String tableName) {
        primary = new DBTrackEventWriter(tableName);
        fallbackWriter = new FileTrackWriter("track.err");
    }

    @Override
    public void write(TrackPoint point) {
        if (!db.write(primary, new TrackEvent(point))) {
            fallbackWriter.write(point);
        }
    }
    
    @Override
    public boolean init() {
        try {
            db = new DBHelper(true);
            return true;
        } catch (Exception e) {
            ServerLog.getLogger().error("Cannot initialize track db!", e);
            return false;
        }
    }

    @Override
    public void dispose() {
        db.close();
        db = null;
    }
}
