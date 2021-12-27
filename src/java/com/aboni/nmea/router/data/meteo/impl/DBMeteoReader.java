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

import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.data.Sample;
import com.aboni.nmea.router.data.meteo.MeteoManagementException;
import com.aboni.nmea.router.data.meteo.MeteoReader;
import com.aboni.utils.db.DBHelper;

import javax.validation.constraints.NotNull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class DBMeteoReader implements MeteoReader {

    private static final String SQL_TIME = "select * from meteo where TS>=? and TS<?";
    private static final String SQL_TIME_AND_TYPE = "select * from meteo where TS>=? and TS<? and type=?";

    @Override
    public void readMeteo(@NotNull Instant from, @NotNull Instant to, @NotNull MeteoReader.MeteoReaderListener target) throws MeteoManagementException {
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement st = db.getConnection().prepareStatement(SQL_TIME)) {
                st.setTimestamp(1, new Timestamp(from.toEpochMilli()));
                st.setTimestamp(2, new Timestamp(to.toEpochMilli()));
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        target.onRead(getSample(rs));
                    }
                }
            }
        } catch (ClassNotFoundException | MalformedConfigurationException | SQLException e) {
            throw new MeteoManagementException("Error reading meteo", e);
        }

    }

    @Override
    public void readMeteo(@NotNull Instant from, @NotNull Instant to, @NotNull String tag, @NotNull MeteoReader.MeteoReaderListener target) throws MeteoManagementException {
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement st = db.getConnection().prepareStatement(SQL_TIME_AND_TYPE)) {
                st.setTimestamp(1, new Timestamp(from.toEpochMilli()));
                st.setTimestamp(2, new Timestamp(to.toEpochMilli()));
                st.setString(3, tag);
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        target.onRead(getSample(rs));
                    }
                }
            }
        } catch (ClassNotFoundException | MalformedConfigurationException | SQLException e) {
            throw new MeteoManagementException("Error reading meteo", e);
        }

    }

    private Sample getSample(ResultSet rs) throws SQLException {
        return Sample.newInstance(
                rs.getTimestamp("TS").getTime(),
                rs.getString("type"),
                rs.getDouble("vMin"),
                rs.getDouble("v"),
                rs.getDouble("vMax"));
    }
}
