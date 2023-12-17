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

package com.aboni.nmea.router.data.sampledquery.impl;

import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.data.Query;
import com.aboni.nmea.router.data.QueryByDate;
import com.aboni.nmea.router.data.QueryById;
import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.data.sampledquery.Range;
import com.aboni.nmea.router.data.sampledquery.RangeFinder;
import com.aboni.nmea.router.data.sampledquery.SampledQueryException;
import com.aboni.log.Log;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.utils.Utils;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

public class DBRangeFinder implements RangeFinder {

    private final String tripTable;
    private final Log log;

    @Inject
    public DBRangeFinder(Log log, @Named(Constants.TAG_TRIP) String tripTable) {
        this.log = log;
        this.tripTable = tripTable;
    }

    private String getSQL(String table, Query q) {
        if (q instanceof QueryByDate) {
            return "select count(TS), max(TS), min(TS) from " + table + " where TS>=? and TS<=?";
        } else if (q instanceof QueryById) {
            return "select count(TS), max(TS), min(TS) from " + table + " " +
                    "where TS>=(select fromTS from " + tripTable + " where id=?) " +
                    "and TS<=(select toTS from " + tripTable + " where id=?)";
        } else {
            return null;
        }
    }

    private void fillStatement(PreparedStatement stm, Query q) throws SQLException {
        if (q instanceof QueryByDate) {
            stm.setTimestamp(1, new java.sql.Timestamp(((QueryByDate) q).getFrom().toEpochMilli()), Utils.UTC_CALENDAR);
            stm.setTimestamp(2, new java.sql.Timestamp(((QueryByDate) q).getTo().toEpochMilli()), Utils.UTC_CALENDAR);
        } else if (q instanceof QueryById) {
            stm.setInt(1, ((QueryById) q).getId());
            stm.setInt(2, ((QueryById) q).getId());
        }
    }

    @Override
    public Range getRange(String table, Query q) throws SampledQueryException {
        String sql = getSQL(table, q);
        if (sql != null) {
            try (DBHelper h = new DBHelper(log, true)) {
                AtomicReference<Range> result = new AtomicReference<>();
                h.executeQuery(sql, (PreparedStatement st) -> fillStatement(st, q),
                        (ResultSet rs) -> {
                            if (rs.next()) {
                                long count = rs.getLong(1);
                                Instant tMax = rs.getTimestamp(2, Utils.UTC_CALENDAR).toInstant();
                                Instant tMin = rs.getTimestamp(3, Utils.UTC_CALENDAR).toInstant();
                                if (tMax != null && tMin != null) {
                                    result.set(new Range(tMax, tMin, count));
                                }
                            }
                        });
                return result.get();
            } catch (SQLException | MalformedConfigurationException e) {
                throw new SampledQueryException("Cannot create time range for {" + table + "}", e);
            }
        } else {
            throw new SampledQueryException("Unsupported query type " + q);
        }
    }
}
