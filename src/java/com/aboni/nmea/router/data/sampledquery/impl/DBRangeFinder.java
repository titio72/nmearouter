package com.aboni.nmea.router.data.sampledquery.impl;

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.data.sampledquery.Range;
import com.aboni.nmea.router.data.sampledquery.RangeFinder;
import com.aboni.utils.Query;
import com.aboni.utils.QueryByDate;
import com.aboni.utils.QueryById;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class DBRangeFinder implements RangeFinder {

    private final String tripTable;

    @Inject
    public DBRangeFinder(@Named(Constants.TAG_TRIP) String tripTable) {
        this.tripTable = tripTable;
    }

    private PreparedStatement getTimeFrameSQL(String table, Query q, DBHelper h) throws SQLException {
        if (q instanceof QueryByDate) {
            String sql = "select count(TS), max(TS), min(TS) from " + table + " where TS>=? and TS<=?";
            PreparedStatement stm = h.getConnection().prepareStatement(sql);
            stm.setTimestamp(1, new java.sql.Timestamp(((QueryByDate) q).getFrom().toEpochMilli()));
            stm.setTimestamp(2, new java.sql.Timestamp(((QueryByDate) q).getTo().toEpochMilli()));
            return stm;
        } else if (q instanceof QueryById) {
            String sql = "select count(TS), max(TS), min(TS) from " + table + " " +
                    "where TS>=(select fromTS from " + tripTable + " where id=?) " +
                    "and TS<=(select toTS from " + tripTable + " where id=?)";
            PreparedStatement stm = h.getConnection().prepareStatement(sql);
            stm.setInt(1, ((QueryById) q).getId());
            stm.setInt(2, ((QueryById) q).getId());
            return stm;
        } else {
            return null;
        }
    }

    @Override
    public Range getRange(String table, Query q) {
        try (DBHelper h = new DBHelper(true)) {
            try (PreparedStatement stm = getTimeFrameSQL(table, q, h)) {
                if (stm != null) {
                    try (ResultSet rs = stm.executeQuery()) {
                        if (rs.next()) {
                            long count = rs.getLong(1);
                            Instant tMax = rs.getTimestamp(2).toInstant();
                            Instant tMin = rs.getTimestamp(3).toInstant();
                            if (tMax != null && tMin != null) {
                                return new Range(tMax, tMin, count);
                            }
                        }
                    }
                } else {
                    ServerLog.getLogger().error("SampledQuery: Unsupported query type " + q);
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            ServerLog.getLogger().error("SampledQuery: Cannot create time range for {" + table + "} because connection is not established!", e);
        }
        return null;
    }

}
