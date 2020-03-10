package com.aboni.nmea.router.meteo.impl;

import com.aboni.nmea.router.meteo.MeteoManagementException;
import com.aboni.nmea.router.meteo.MeteoReader;
import com.aboni.nmea.router.meteo.MeteoSample;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;

import javax.validation.constraints.NotNull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class DBMeteoReader implements MeteoReader {

    private static final String SQL_TIME = "select * from meteo where TS>=? and TS<?";

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
        } catch (ClassNotFoundException | SQLException e) {
            ServerLog.getLogger().error("Error reading meteo", e);
            throw new MeteoManagementException("Error reading meteo", e);
        }

    }

    private MeteoSample getSample(ResultSet rs) throws SQLException {
        return MeteoSample.newInstance(
                rs.getTimestamp("TS").getTime(),
                rs.getString("type"),
                rs.getDouble("vMin"),
                rs.getDouble("v"),
                rs.getDouble("vMax"));
    }
}
