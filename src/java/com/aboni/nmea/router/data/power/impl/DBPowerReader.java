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

package com.aboni.nmea.router.data.power.impl;

import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.log.Log;
import com.aboni.log.SafeLog;
import com.aboni.nmea.router.data.*;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.utils.Utils;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class DBPowerReader implements DataReader {

    private static final String SQL_TIME = "select * from power where TS>=? and TS<?";
    private static final String SQL_TIME_AND_TYPE = "select * from power where TS>=? and TS<? and type=?";
    private final Log log;

    @Inject
    public DBPowerReader(Log log) {
        this.log = SafeLog.getSafeLog(log);
    }

    @Override
    public void readData(Query query, DataReader.DataReaderListener target) throws DataManagementException {
        if (target == null) throw new IllegalArgumentException("Results target is null");
        if (query instanceof QueryByDate) {
            Instant from = ((QueryByDate) query).getFrom();
            Instant to = ((QueryByDate) query).getTo();
            try (DBHelper db = new DBHelper(log, true)) {
                db.executeQuery(SQL_TIME,
                        (PreparedStatement st) -> {
                            st.setTimestamp(1, new Timestamp(from.toEpochMilli()), Utils.UTC_CALENDAR);
                            st.setTimestamp(2, new Timestamp(to.toEpochMilli()), Utils.UTC_CALENDAR);
                        },
                        (ResultSet rs)->readResults(rs, target));
            } catch (MalformedConfigurationException | SQLException e) {
                throw new DataManagementException("Error reading meteo", e);
            }
        } else {
            throw new DataManagementException("Unsupported query");
        }
    }

    @Override
    public void readData(Query query, String tag, DataReader.DataReaderListener target) throws DataManagementException {
        if (target == null) throw new IllegalArgumentException("Results target is null");
        if (tag == null) throw new IllegalArgumentException("Query tag is null");
        if (query instanceof QueryByDate) {
            Instant from = ((QueryByDate) query).getFrom();
            Instant to = ((QueryByDate) query).getTo();
            try (DBHelper db = new DBHelper(log, true)) {
                db.executeQuery(SQL_TIME_AND_TYPE,
                        (PreparedStatement st) -> {
                            st.setTimestamp(1, new Timestamp(from.toEpochMilli()), Utils.UTC_CALENDAR);
                            st.setTimestamp(2, new Timestamp(to.toEpochMilli()), Utils.UTC_CALENDAR);
                            st.setString(3, tag);
                        },
                        (ResultSet rs)->readResults(rs, target));
            } catch (MalformedConfigurationException | SQLException e) {
                throw new DataManagementException("Error reading power", e);
            }
        } else {
            throw new DataManagementException("Unsupported query");
        }
    }

    private void readResults(ResultSet rs, DataReaderListener target) throws SQLException {
        while (rs.next()) {
            target.onRead(getSample(rs));
        }
    }

    private Sample getSample(ResultSet rs) throws SQLException {
        return ImmutableSample.newInstance(
                rs.getTimestamp("TS", Utils.UTC_CALENDAR).getTime(),
                rs.getString("type"),
                rs.getDouble("v"),
                rs.getDouble("v"),
                rs.getDouble("v"));
    }
}
