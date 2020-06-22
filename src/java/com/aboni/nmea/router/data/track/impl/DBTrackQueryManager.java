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

import com.aboni.nmea.router.data.track.TrackManagementException;
import com.aboni.nmea.router.data.track.TrackQueryManager;
import com.aboni.utils.db.DBHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBTrackQueryManager implements TrackQueryManager {

    private static final String SQL_YEAR_STATS = "select year(TS), month(TS), sum(dist*(1-anchor) 	), sum(dTime*(1-anchor)), count(distinct day(TS)) from track group by year(TS), month(TS)";

    @Override
    public JSONObject getYearlyStats() throws TrackManagementException {
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement stm = db.getConnection().prepareStatement(SQL_YEAR_STATS)) {
                JSONObject res = new JSONObject();
                JSONArray samples = new JSONArray();
                try (ResultSet rs = stm.executeQuery()) {
                    int lastM = 0;
                    int lastY = 0;
                    while (rs.next()) {
                        int y = rs.getInt(1);
                        if (lastY < y && lastY > 0) {
                            for (int i = lastM + 1; i <= 12; i++) {
                                JSONArray e = new JSONArray(new Object[]{lastY, i, 0.0, 0, 0});
                                samples.put(e);
                            }
                            lastM = 0;
                        }
                        lastY = y;

                        int m = rs.getInt(2);
                        if ((m - lastM) > 1) {
                            for (int i = lastM + 1; i < m; i++) {
                                JSONArray e = new JSONArray(new Object[]{y, i, 0.0, 0, 0});
                                samples.put(e);
                            }
                        }
                        lastM = m;
                        double dist = rs.getDouble(3);
                        double sailTime = rs.getDouble(4);
                        double days = rs.getDouble(5);
                        JSONArray e = new JSONArray(new Object[]{y, m, dist, sailTime / 3600, days});
                        samples.put(e);
                    }
                }
                res.put("NM_per_month", samples);
                return res;
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new TrackManagementException("Error reading distance stats", e);
        }
    }

}
