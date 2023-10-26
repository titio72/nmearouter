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

import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.data.track.TrackManagementException;
import com.aboni.nmea.router.data.track.MonthYearTrackStats;
import com.aboni.nmea.router.utils.Log;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.utils.ScanThrough;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class DBMonthYearTrackStats implements MonthYearTrackStats {

    private static class YearMonth implements Comparable<YearMonth> {

        /**
         * Year in YYYY format
         */
        final int y;

        /**
         * Month 1-12
         */
        final int m;

        YearMonth(int y, int m) {
            this.y = y;
            this.m = m;
        }

        @Override
        public int compareTo(YearMonth yearMonth) {
            return toInt().compareTo(yearMonth.toInt());
        }

        @Override
        public boolean equals(Object x) {
            return super.equals(x);
        }

        @Override
        public int hashCode() {
            return toInt();
        }

        Integer toInt() {
            return y*100+m;
        }
        YearMonth addMonth() {
            if (m==12) {
                return new YearMonth(y+1, 1);
            } else {
                return new YearMonth(y, m+1);
            }
        }

        static YearMonth getNow() {
            LocalDate d = LocalDate.now();
            return new YearMonth(d.getYear(), d.getMonthValue());
        }
    }

    private static class SailStats {

        YearMonth key;
        double dist;
        double distSail;
        double distMotor;
        double distUnknown;

        /**
         * Navigation time in seconds
         */
        double sailTime;
        double days;

        SailStats(YearMonth k) {
            key = k;
            dist = 0;
            sailTime = 0;
            distSail = 0;
            distMotor = 0;
            distUnknown = 0;
            days = 0;
        }

        SailStats(ResultSet rs) throws SQLException{
            key = new YearMonth(rs.getInt(1), rs.getInt(2));
            dist = rs.getDouble(3);
            sailTime = rs.getDouble(4);
            distSail = rs.getDouble(5);
            distMotor = rs.getDouble(6);
            distUnknown = rs.getDouble(7);
            days = rs.getDouble(8);
        }

        YearMonth getKey() {
            return key;
        }

        SailStats add(SailStats s) {
            dist += s.dist;
            distSail += s.distSail;
            distMotor += s.distMotor;
            distUnknown += s.distUnknown;
            sailTime += s.sailTime;
            days += s.days;
            return this;
        }

        JSONObject toJSON() {
            JSONObject res = new JSONObject();
            res.put("Y", key.y);
            res.put("M", key.m);
            res.put("dist", dist);
            res.put("distSail", distSail);
            res.put("distMotor", distMotor);
            res.put("distUnknown", distUnknown);
            res.put("hours", sailTime/3600);
            res.put("days", days);
            return res;
        }
    }

    private static final String SQL_YEAR_STATS = "select " +
            "year(TS) as Y, month(TS) as M, " +
            "sum(dist*(1-anchor)), " +
            "sum(dTime*(1-anchor)), " +
            "sum(dist*(1-anchor)*if(engine=0,1,0)), " +
            "sum(dist*(1-anchor)*if(engine=1,1,0)), " +
            "sum(dist*(1-anchor)*if(engine=2,1,0)), " +
            "count(distinct day(TS)) from track group by year(TS), month(TS) " +
            "order by Y, M ";
    private final Log log;

    @Inject
    public DBMonthYearTrackStats(Log log) {
        this.log = log;
    }

    @Override
    public JSONObject getYearlyStats() throws TrackManagementException {
        try (DBHelper helper = new DBHelper(log,true)) {
            JSONObject res = new JSONObject();
            helper.executeQuery(SQL_YEAR_STATS, (ResultSet rs)-> fillResults(rs, res));
            return res;
        } catch (SQLException | ClassNotFoundException | MalformedConfigurationException e) {
            throw new TrackManagementException("Error reading distance stats", e);
        }
    }

    private void fillResults(ResultSet rs, JSONObject res) throws SQLException {
        YearMonth last = null;
        JSONArray monthlyStats = new JSONArray();
        ScanThrough<SailStats, Integer, SailStats> aggregator = new ScanThrough<>(
                (SailStats s)->s.getKey().y,
                (Integer y)->new SailStats(new YearMonth(y, 12)),
                (SailStats ms, Integer y, SailStats yearStats)->yearStats.add(ms)
        );
        while (rs.next()) {
            SailStats ms = new SailStats(rs);
            YearMonth k = ms.getKey();
            fillMonthsGap(last, k, monthlyStats);
            monthlyStats.put(ms.toJSON());
            aggregator.processItem(ms);
            last = k;
        }
        fillMonthsGap(last, YearMonth.getNow().addMonth(), monthlyStats);
        JSONArray yearlyStats = new JSONArray();
        for (SailStats yearStats: aggregator.getResults().values()) {
            yearlyStats.put(yearStats.toJSON());
        }
        res.put("NM_per_month", monthlyStats);
        res.put("NM_per_year", yearlyStats);
    }

    private static void fillMonthsGap(YearMonth from, YearMonth to, JSONArray samples) {
        if (from!=null && to!=null && samples!=null) {
            for (YearMonth i = from.addMonth(); i.compareTo(to) < 0; i = i.addMonth()) {
                samples.put(new SailStats(i).toJSON());
            }
        }
    }
}
