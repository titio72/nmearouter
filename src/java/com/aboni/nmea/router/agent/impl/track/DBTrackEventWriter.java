package com.aboni.nmea.router.agent.impl.track;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.Event;
import com.aboni.utils.db.EventWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DBTrackEventWriter implements EventWriter {

    static String sTABLE = "track";

	private PreparedStatement stm;
    private PreparedStatement stmTrip;

	public DBTrackEventWriter() {
		// nothing to init
	}

	private void prepareStatement(Connection c) throws SQLException {
		if (stm==null) {
            stm = c.prepareStatement("insert into " + sTABLE + " (lat, lon, TS, anchor, dTime, speed, maxSpeed, dist, engine) values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        }
    }

    private void prepareStatementWithTrip(Connection c) throws SQLException {
        if (stmTrip == null) {
            stmTrip = c.prepareStatement("insert into " + sTABLE + " (lat, lon, TS, anchor, dTime, speed, maxSpeed, dist, engine, tripId) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        }
    }

    private PreparedStatement closeStatement(PreparedStatement s) {
        if (s != null) {
            try {
                s.close();
            } catch (Exception e) {
                ServerLog.getLogger().error("Error closing statement in " + getClass().getSimpleName(), e);
            }
        }
        return null;
    }

    @Override
    public void reset() {
        stm = closeStatement(stm);
        stmTrip = closeStatement(stmTrip);
	}
	
	@Override
	public void write(Event e, Connection conn) throws SQLException{
		if (conn!=null && e instanceof TrackEvent) {
            PreparedStatement s;
            if (((TrackEvent) e).getPoint().getTrip() == null) {
                prepareStatement(conn);
                s = stm;
            } else {
                prepareStatementWithTrip(conn);
                s = stmTrip;
            }
        	TrackEvent t = (TrackEvent)e;
            s.setDouble(1, t.getPoint().getPosition().getLatitude());
            s.setDouble(2, t.getPoint().getPosition().getLongitude());
            Timestamp x = new Timestamp(e.getTime());
            s.setTimestamp(3, x);
            s.setInt(4, t.getPoint().isAnchor() ? 1 : 0);
            s.setInt(5, t.getPoint().getPeriod());
            s.setDouble(6, t.getPoint().getAverageSpeed());
            s.setDouble(7, Math.max(t.getPoint().getMaxSpeed(), t.getPoint().getAverageSpeed()));
            s.setDouble(8, t.getPoint().getDistance());
            s.setByte(9, t.getPoint().getEngine().toByte());
            if (((TrackEvent) e).getPoint().getTrip() != null) {
                s.setInt(10, t.getPoint().getTrip());
            }
            s.execute();
	    }
	}
}
