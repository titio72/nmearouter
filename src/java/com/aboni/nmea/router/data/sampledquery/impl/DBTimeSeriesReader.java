package com.aboni.nmea.router.data.sampledquery.impl;

import com.aboni.nmea.router.data.sampledquery.Range;
import com.aboni.nmea.router.data.sampledquery.SampledQueryConf;
import com.aboni.nmea.router.data.sampledquery.TimeSeriesReader;
import com.aboni.utils.ServerLog;
import com.aboni.utils.TimeSeries;
import com.aboni.utils.db.DBHelper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class DBTimeSeriesReader implements TimeSeriesReader {

    @Override
    public Map<String, TimeSeries> getTimeSeries(SampledQueryConf conf, int maxSamples, Range range) {
        try (DBHelper db = new DBHelper(true)) {
            Map<String, TimeSeries> res = new HashMap<>();
            Timestamp cFrom = new Timestamp(range.getMin().toEpochMilli());
            Timestamp cTo = new Timestamp(range.getMax().toEpochMilli());
            int sampling = range.getSampling(maxSamples);
            String sql = getTimeSeriesSQL(conf);
            try (PreparedStatement stm = db.getConnection().prepareStatement(sql)) {
                stm.setTimestamp(1, cFrom);
                stm.setTimestamp(2, cTo);
                readSamples(res, stm, sampling, maxSamples);
            }
            return res;
        } catch (ClassNotFoundException | SQLException e) {
            ServerLog.getLogger().error("TimeSeriesReader: cannot read time series", e);
            return null;
        }
    }

    private static void readSamples(Map<String, TimeSeries> res, PreparedStatement stm, int sampling, int maxSamples) throws SQLException {
        try (ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp(1);
                String type = rs.getString(2);
                double vMax = rs.getDouble(3);
                double v = rs.getDouble(4);
                double vMin = rs.getDouble(5);
                TimeSeries timeSeries = res.getOrDefault(type, null);
                if (timeSeries == null) {
                    timeSeries = new TimeSeries(sampling, maxSamples);
                    res.put(type, timeSeries);
                }
                timeSeries.doSampling(ts.getTime(), vMax, v, vMin);
            }
        }
    }

    private static String getTimeSeriesSQL(SampledQueryConf conf) {
        StringBuilder sqlBuilder = new StringBuilder("select TS, ");
        if (conf.getSeriesNameField() != null) {
            sqlBuilder.append(String.format("%s as ___sName, ", conf.getSeriesNameField()));
        } else {
            sqlBuilder.append(String.format("'%S' as ___sName, ", conf.getSeriesName()));
        }
        sqlBuilder.append(conf.getMaxField()).append(", ");
        sqlBuilder.append(conf.getAvgField()).append(", ");
        sqlBuilder.append(conf.getMinField());
        sqlBuilder.append(String.format(" from %s where TS>=? and TS<=? order by TS", conf.getTable()));
        return sqlBuilder.toString();
    }
}
