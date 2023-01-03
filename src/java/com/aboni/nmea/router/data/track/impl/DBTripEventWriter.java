/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.data.track.impl;

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.data.track.TripEvent;
import com.aboni.nmea.router.utils.db.DBEventWriter;
import com.aboni.nmea.router.utils.db.Event;

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
    public DBTripEventWriter(@NotNull @Named(Constants.TAG_TRIP) String tableName) {
        sTABLE = tableName;
    }

    private void prepareStatement(Connection c) throws SQLException {
        if (stm == null) {
            stm = c.prepareStatement("UPDATE " + sTABLE + " set toTS=?, dist=? where id=?");
        }
    }

    private void closeStatement(PreparedStatement s) throws SQLException {
        if (s != null) {
            s.close();
        }
    }

    @Override
    public void reset() throws SQLException {
        closeStatement(stm);
        stm = null;
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