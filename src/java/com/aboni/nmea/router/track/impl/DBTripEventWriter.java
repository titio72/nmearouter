package com.aboni.nmea.router.track.impl;

import com.aboni.nmea.router.track.TripEvent;
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

public class DBTripEventWriter implements DBEventWriter {

    private final String sTABLE;

    private PreparedStatement stm;
    private Connection lastUsedConnection;

    @Inject
    public DBTripEventWriter(@NotNull @Named("TripTableName") String tableName) {
        sTABLE = tableName;
    }

    private void prepareStatement(Connection c) throws SQLException {
        if (stm == null) {
            stm = c.prepareStatement("UPDATE " + sTABLE + " set toTS=?, dist=? where id=?");
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
    }

    @Override
    public void write(Event e, Connection conn) throws SQLException {
        if (conn != null && e instanceof TripEvent) {
            if (conn != lastUsedConnection) reset();
            lastUsedConnection = conn;
            prepareStatement(conn);
            TripEvent t = (TripEvent) e;
            stm.setTimestamp(1, new Timestamp(t.getTime()));
            stm.setDouble(2, t.getTrip().getDistance());
            stm.setInt(3, t.getTrip().getTrip());
            stm.execute();
        }
    }
}