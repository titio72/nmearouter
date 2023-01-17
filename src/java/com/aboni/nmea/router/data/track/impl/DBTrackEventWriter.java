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
import com.aboni.nmea.router.data.track.TrackEvent;
import com.aboni.nmea.router.utils.db.DBEventWriter;
import com.aboni.nmea.router.utils.db.Event;
import com.aboni.utils.Utils;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DBTrackEventWriter implements DBEventWriter {

    private final String sTABLE;

    private PreparedStatement stm;
    private Connection lastUsedConnection;

    @Inject
    public DBTrackEventWriter(@Named(Constants.TAG_TRACK) String tableName) {
        sTABLE = tableName;
    }

    private void prepareStatement(Connection c) throws SQLException {
        if (stm == null) {
            stm = c.prepareStatement("insert into " + sTABLE + " (lat, lon, TS, anchor, dTime, speed, maxSpeed, dist, engine) values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
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
    public void write(Event e, Connection conn) throws SQLException{
        if (conn!=null && e instanceof TrackEvent) {
            if (conn != lastUsedConnection) reset();
            lastUsedConnection = conn;
            prepareStatement(conn);
            TrackEvent t = (TrackEvent) e;
            stm.setDouble(1, t.getPoint().getPosition().getLatitude());
            stm.setDouble(2, t.getPoint().getPosition().getLongitude());
            Timestamp x = new Timestamp(e.getTime());
            stm.setTimestamp(3, x, Utils.UTC_CALENDAR);
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
