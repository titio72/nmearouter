package com.aboni.nmea.router.agent.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.aboni.utils.db.Event;
import com.aboni.utils.db.EventWriter;

public class DBTrackEventWriter implements EventWriter {

	private PreparedStatement stm;
	
	public DBTrackEventWriter() {
	}
	
	private void prepareStatement(Connection c) throws SQLException {
		if (stm==null) {
			stm = c.prepareStatement("insert into track (lat, lon, TS, anchor, dTime, speed, maxSpeed, dist) values (?, ?, ?, ?, ?, ?, ?, ?)");
        }
	}
	
	@Override
	public void reset() {
		try {
			stm.close();
		} catch (Exception e) {}
		stm = null;
	}
	
	@Override
	public void write(Event e, Connection conn) throws SQLException{
		if (conn!=null && e instanceof TrackEvent) {
			prepareStatement(conn);
        	TrackEvent t = (TrackEvent)e;
            stm.setDouble(1, t.getP().getLatitude());
            stm.setDouble(2, t.getP().getLongitude());
            Timestamp x = new Timestamp(t.getTime());
            stm.setTimestamp(3, x);
            stm.setInt(4, t.isAnchor()?1:0);
            stm.setInt(5, t.getInterval());
            stm.setDouble(6, t.getSpeed());
            stm.setDouble(7, Math.max(t.getMaxSpeed(), t.getSpeed()));
            stm.setDouble(8, t.getDist());
            stm.execute();
	    }
	}
}
