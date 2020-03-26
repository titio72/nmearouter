package com.aboni.nmea.router.data.track.impl;

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.data.track.TrackEvent;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBEventWriter;
import com.aboni.utils.db.Event;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DBTrackEventWriter implements DBEventWriter {

    private final String sTABLE;

    private PreparedStatement stm;
    private Connection lastUsedConnection;

    @Inject
    public DBTrackEventWriter(@NotNull @Named(Constants.TAG_TRACK) String tableName) {
        sTABLE = tableName;
    }

    private void prepareStatement(Connection c) throws SQLException {
        if (stm == null) {
            stm = c.prepareStatement("insert into " + sTABLE + " (lat, lon, TS, anchor, dTime, speed, maxSpeed, dist, engine) values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        }
    }

    private void closeStatement(PreparedStatement s) {
        if (s != null) {
            try {
                s.close();
            } catch (Exception e) {
                ServerLog.getLogger().error("Error closing statement in " + getClass().getSimpleName(), e);
            }
        }
    }

    @Override
    public void reset() {
        closeStatement(stm);
        stm = null;
    }
	
	@Override
	public void write(Event e, Connection conn) throws SQLException{
		if (conn!=null && e instanceof TrackEvent) {
            if (conn != lastUsedConnection) reset();
            lastUsedConnection = conn;
            prepareStatement(conn);
            TrackEvent t = (TrackEvent) e;
            stm.setDouble(1, t.getPoint().getPosition().getLatitude());
            stm.setDouble(2, t.getPoint().getPosition().getLongitude());
            Timestamp x = new Timestamp(e.getTime());
            stm.setTimestamp(3, x);
            stm.setInt(4, t.getPoint().isAnchor() ? 1 : 0);
            stm.setInt(5, t.getPoint().getPeriod());
            stm.setDouble(6, t.getPoint().getAverageSpeed());
            stm.setDouble(7, Math.max(t.getPoint().getMaxSpeed(), t.getPoint().getAverageSpeed()));
            stm.setDouble(8, t.getPoint().getDistance());
            stm.setByte(9, t.getPoint().getEngine().toByte());
            stm.execute();
        }
	}
}
