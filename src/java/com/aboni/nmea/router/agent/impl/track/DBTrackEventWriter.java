package com.aboni.nmea.router.agent.impl.track;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.Event;
import com.aboni.utils.db.EventWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DBTrackEventWriter implements EventWriter {

	private PreparedStatement stm;
    private PreparedStatement stmTrip;

	public DBTrackEventWriter() {
		// nothing to init
	}

	private void prepareStatement(Connection c) throws SQLException {
		if (stm==null) {
			stm = c.prepareStatement("insert into track (lat, lon, TS, anchor, dTime, speed, maxSpeed, dist) values (?, ?, ?, ?, ?, ?, ?, ?)");
        }
    }

    private void prepareStatementWithTrip(Connection c) throws SQLException {
        if (stmTrip == null) {
            stmTrip = c.prepareStatement("insert into track (lat, lon, TS, anchor, dTime, speed, maxSpeed, dist, tripId) values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        }
    }

	@Override
	public void reset() {
		try {
			stm.close();
		} catch (Exception e) {
			ServerLog.getLogger().error("Error closing statement in " + getClass().getSimpleName(), e);
		}
		stm = null;
	}
	
	@Override
	public void write(Event e, Connection conn) throws SQLException{
		if (conn!=null && e instanceof TrackEvent) {
            if (((TrackEvent) e).getPoint().getTrip() == null) {
                prepareStatement(conn);
            } else {
                prepareStatementWithTrip(conn);
            }
        	TrackEvent t = (TrackEvent)e;
            stm.setDouble(1, t.getPoint().getPosition().getLatitude());
            stm.setDouble(2, t.getPoint().getPosition().getLongitude());
            Timestamp x = new Timestamp(e.getTime());
            stm.setTimestamp(3, x);
            stm.setInt(4, t.getPoint().isAnchor() ? 1 : 0);
            stm.setInt(5, t.getPoint().getPeriod());
            stm.setDouble(6, t.getPoint().getAverageSpeed());
            stm.setDouble(7, Math.max(t.getPoint().getMaxSpeed(), t.getPoint().getAverageSpeed()));
            stm.setDouble(8, t.getPoint().getDistance());
            if (((TrackEvent) e).getPoint().getTrip() != null) {
                stm.setInt(9, t.getPoint().getTrip());
            }
            stm.execute();
	    }
	}
}
