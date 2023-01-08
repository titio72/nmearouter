/*
 * Copyright (c) 2022,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.data.power.impl;

import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.data.DataManagementException;
import com.aboni.nmea.router.data.power.PowerAnalytics;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class DBPowerAnalytics implements PowerAnalytics {

    private static final String SQL = /*"set @total:=0;\r\n" +*/
            "select T.t, T.AHh, @total := @total + T.AHh AS AHt from (" +
                    "select " +
                    "  FROM_UNIXTIME(round(UNIX_TIMESTAMP(TS)/?)*?) as t, " +
                    "  sum(v) as AHh " +
                    "  from power where type='C_0' and TS>=? and TS<=? " +
                    "group by" +
                    "  FROM_UNIXTIME(round(UNIX_TIMESTAMP(TS)/?)*?)) T;";

    @Override
    public JSONObject getPowerUsage(int samplingPeriod, Instant from, Instant to) throws DataManagementException {
        try (DBHelper db = new DBHelper(true)) {
            JSONObject res = new JSONObject();
            JSONArray a = new JSONArray();
            try (PreparedStatement st = db.getConnection().prepareStatement(SQL)) {
                st.executeQuery("set @total:=0;");
                st.setInt(1, samplingPeriod);
                st.setInt(2, samplingPeriod);
                st.setTimestamp(3, new Timestamp(from.toEpochMilli()), Utils.UTC_CALENDAR);
                st.setTimestamp(4, new Timestamp(to.toEpochMilli()), Utils.UTC_CALENDAR);
                st.setInt(5, samplingPeriod);
                st.setInt(6, samplingPeriod);
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        long t = rs.getTimestamp(1, Utils.UTC_CALENDAR).getTime();
                        double aH = rs.getDouble(2);
                        double aHT = rs.getDouble(3);
                        JSONObject s = new JSONObject();
                        s.put("t", t);
                        s.put("Ah", aH);
                        s.put("AhT", aHT);
                        a.put(s);
                    }
                }
                res.put("C_0", a);
            }
            return res;
        } catch (ClassNotFoundException | MalformedConfigurationException | SQLException e) {
            throw new DataManagementException("Error reading power", e);
        }
    }
}
