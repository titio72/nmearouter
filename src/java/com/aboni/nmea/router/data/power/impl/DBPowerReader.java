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
import com.aboni.nmea.router.data.DataManagementException;
import com.aboni.nmea.router.data.DataReader;
import com.aboni.nmea.router.data.ImmutableSample;
import com.aboni.nmea.router.data.Sample;
import com.aboni.nmea.router.utils.Query;
import com.aboni.nmea.router.utils.QueryByDate;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.utils.Utils;

import javax.validation.constraints.NotNull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class DBPowerReader implements DataReader {

    private static final String SQL_TIME = "select * from power where TS>=? and TS<?";
    private static final String SQL_TIME_AND_TYPE = "select * from power where TS>=? and TS<? and type=?";

    @Override
    public void readData(@NotNull Query query, @NotNull DataReader.DataReaderListener target) throws DataManagementException {
        if (query instanceof QueryByDate) {
            Instant from = ((QueryByDate) query).getFrom();
            Instant to = ((QueryByDate) query).getTo();
            try (DBHelper db = new DBHelper(true)) {
                try (PreparedStatement st = db.getConnection().prepareStatement(SQL_TIME)) {
                    st.setTimestamp(1, new Timestamp(from.toEpochMilli()), Utils.UTC_CALENDAR);
                    st.setTimestamp(2, new Timestamp(to.toEpochMilli()), Utils.UTC_CALENDAR);
                    try (ResultSet rs = st.executeQuery()) {
                        while (rs.next()) {
                            target.onRead(getSample(rs));
                        }
                    }
                }
            } catch (ClassNotFoundException | MalformedConfigurationException | SQLException e) {
                throw new DataManagementException("Error reading meteo", e);
            }
        } else {
            throw new DataManagementException("Unsupported query");
        }
    }

    @Override
    public void readData(@NotNull Query query, @NotNull String tag, @NotNull DataReader.DataReaderListener target) throws DataManagementException {
        if (query instanceof QueryByDate) {
            Instant from = ((QueryByDate) query).getFrom();
            Instant to = ((QueryByDate) query).getTo();
            try (DBHelper db = new DBHelper(true)) {
                try (PreparedStatement st = db.getConnection().prepareStatement(SQL_TIME_AND_TYPE)) {
                    st.setTimestamp(1, new Timestamp(from.toEpochMilli()), Utils.UTC_CALENDAR);
                    st.setTimestamp(2, new Timestamp(to.toEpochMilli()), Utils.UTC_CALENDAR);
                    st.setString(3, tag);
                    try (ResultSet rs = st.executeQuery()) {
                        while (rs.next()) {
                            target.onRead(getSample(rs));
                        }
                    }
                }
            } catch (ClassNotFoundException | MalformedConfigurationException | SQLException e) {
                throw new DataManagementException("Error reading power", e);
            }
        } else {
            throw new DataManagementException("Unsupported query");
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
