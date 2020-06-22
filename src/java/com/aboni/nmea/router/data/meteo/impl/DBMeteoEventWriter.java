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

package com.aboni.nmea.router.data.meteo.impl;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBEventWriter;
import com.aboni.utils.db.Event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DBMeteoEventWriter implements DBEventWriter {

    private PreparedStatement stm;

    public DBMeteoEventWriter() {
        // nothing to initialize
    }

    private void prepareStatement(Connection c) throws SQLException {
        if (stm == null) {
            stm = c.prepareStatement("insert into meteo (type, v, vMax, vMin, TS) values (?, ?, ?, ?, ?)");
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
    public void write(Event e, Connection c) throws SQLException {
        if (c!=null && e instanceof MeteoEvent) {
            prepareStatement(c);
            MeteoEvent m = (MeteoEvent)e;
            stm.setString(1, m.getStatsSample().getTag());
            stm.setDouble(2, m.getStatsSample().getAvg());
            stm.setDouble(3, m.getStatsSample().getMax());
            stm.setDouble(4, m.getStatsSample().getMin());
            stm.setTimestamp(5, new Timestamp(e.getTime()));
            stm.execute();
        }
    }

}
