package com.aboni.nmea.router.services;

import com.aboni.misc.Utils;
import com.aboni.utils.ServerLog;
import com.aboni.utils.TimeSerie;
import com.aboni.utils.TimeSerieSample;
import com.aboni.utils.db.DBHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampledQueryService implements WebService {

    private final SampledQueryConf conf;
    private final SampleWriterFactory sampleWriterFactory;
    private static final int DEFAULT_MAX_SAMPLES = 500;

    public static class SampledQueryConf {
        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }

        public String getMaxField() {
            return maxField;
        }

        public void setMaxField(String maxField) {
            this.maxField = maxField;
        }

        public String getMinField() {
            return minField;
        }

        public void setMinField(String minField) {
            this.minField = minField;
        }

        public String getAvgField() {
            return avgField;
        }

        public void setAvgField(String avgField) {
            this.avgField = avgField;
        }

        public String getSeriesNameField() {
            return seriesNameField;
        }

        public void setSeriesNameField(String seriesNameField) {
            this.seriesNameField = seriesNameField;
        }

        public String getWhere() {
            return where;
        }

        public void setWhere(String where) {
            this.where = where;
        }

        private String table;
        private String maxField;
        private String minField;
        private String avgField;
        private String seriesNameField;
        private String where;
    }

    interface SampleWriter {
        JSONObject[] getSampleNode(TimeSerieSample sample);
    }

    interface SampleWriterFactory {
        SampleWriter getWriter(String type);
    }

    static class DefaultSampleWriter implements SampleWriter {
        @Override
        public JSONObject[] getSampleNode(TimeSerieSample sample) {
            JSONObject s = new JSONObject();
            s.put("time", sample.getT0());
            s.put("vMin", Utils.round(sample.getvMin(), 2));
            s.put("v", Utils.round(sample.getV(), 2));
            s.put("vMax", Utils.round(sample.getvMax(), 2));
            return new JSONObject[]{s};
        }
    }

    SampledQueryService(SampledQueryConf conf, SampleWriterFactory sampleWriterFactory) {
        this.conf = conf;
        this.sampleWriterFactory = sampleWriterFactory;
    }

    private int getMaxSamples(ServiceConfig config) {
        return config.getInteger("samples", DEFAULT_MAX_SAMPLES);
    }

    private class ResultContext {
        final JSONObject res = new JSONObject();
        final Map<String, SampleWriter> sampleWriters = new HashMap<>();
        final SampleWriter defaultWriter = new DefaultSampleWriter();

        SampleWriter getWriter(String type) {
            if (sampleWriterFactory == null)
                return defaultWriter;
            else {
                SampleWriter w = sampleWriters.getOrDefault(type, null);
                if (w == null) {
                    w = sampleWriterFactory.getWriter(type);
                    sampleWriters.put(type, w);
                }
                return w;
            }
        }
    }

    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {

        DateRangeParameter fromTo = new DateRangeParameter(config);
        Calendar cFrom = fromTo.getFrom();
        Calendar cTo = fromTo.getTo();

        int maxSamples = getMaxSamples(config);

        ResultContext ctx = new ResultContext();

        try (DBHelper db = new DBHelper(true)) {
            Range range = getTimeframe(conf.getTable(), cFrom, cTo);
            if (range!=null) {
                Map<String, TimeSerie> timeSerieMap = getTimeSerie(cFrom, cTo, maxSamples, db, range);
                for (Map.Entry<String, TimeSerie> e : timeSerieMap.entrySet()) {
                    fillResponse(ctx, e.getKey(), e.getValue().getSamples());
                }
            } else {
                fillResponse(ctx, "nothing", null);
            }
        } catch (Exception e) {
            ServerLog.getLogger().error("Error writing samples", e);
            ctx.res.put("error", "Error writing samples");
        }

        try {
            response.setContentType("application/json");
            ctx.res.write(response.getWriter());
        } catch (Exception e) {
            ServerLog.getLogger().error("Error writing result to output writer", e);
        }
    }

    private void fillResponse(ResultContext ctx, String type, List<TimeSerieSample> samples) {
        SampleWriter w = ctx.getWriter(type);
        if (w != null) {
            for (TimeSerieSample sample : samples) {
                JSONArray a;
                if (ctx.res.has(type)) {
                    a = ctx.res.getJSONArray(type);
                } else {
                    a = new JSONArray();
                    ctx.res.put(type, a);
                }
                JSONObject[] res = w.getSampleNode(sample);
                if (res != null) {
                    for (JSONObject n : res) {
                        if (n != null) a.put(n);
                    }
                }
            }
        }
    }

    private Map<String, TimeSerie> getTimeSerie(Calendar cFrom, Calendar cTo, int maxSamples, DBHelper db, Range range) throws SQLException {
        Map<String, TimeSerie> res = new HashMap<>();

        int sampling = range.getSampling(maxSamples);
        String sql = getTimeSeriesSQL(conf.getTable(), conf.getSeriesNameField(), new String[]{conf.getMaxField(), conf.getAvgField(), conf.getMinField()}, conf.getWhere());
        try (PreparedStatement stm = db.getConnection().prepareStatement(sql)) {
            stm.setTimestamp(1, new java.sql.Timestamp(cFrom.getTimeInMillis() ));
            stm.setTimestamp(2, new java.sql.Timestamp(cTo.getTimeInMillis() ));
            readSamples(res, stm, sampling, maxSamples);
        }
        return res;
    }

    private void readSamples(Map<String, TimeSerie> res, PreparedStatement stm, int sampling, int maxSamples) throws SQLException {
        try (ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp(1);
                String type = rs.getString(2);
                double vMax = rs.getDouble(3);
                double v = rs.getDouble(4);
                double vMin = rs.getDouble(5);
                TimeSerie timeSerie = res.getOrDefault(type, null);
                if (timeSerie == null) {
                    timeSerie = new TimeSerie(sampling, maxSamples);
                    res.put(type, timeSerie);
                }
                timeSerie.doSampling(ts.getTime(), vMax, v, vMin);
            }
        }
    }

    private static String getTimeSeriesSQL(String table, String serieName, String[] fields, String where) {
        StringBuilder sqlBuilder = new StringBuilder("select TS, " + serieName);
        for (String f : fields) {
            sqlBuilder.append(", ").append(f);
        }
        String sql = sqlBuilder.toString();
        sql += " from " + table + " where TS>=? and TS<=?";
        if (where != null && !where.isEmpty()) {
            sql += " AND " + where;
        }
        sql += " order by TS";
        return sql;
    }

    private static class Range {
        private final Timestamp max;
        private final Timestamp min;
        private final long count;

        Range(Timestamp max, Timestamp min, long count) {
            this.max = max;
            this.min = min;
            this.count = count;
        }

        long getCount() {
            return count;
        }

        long getInterval() {
            return max.getTime() - min.getTime();
        }

        int getSampling(int maxSamples) {
            return (int) ((getCount() <= maxSamples) ? 1 : (getInterval() / maxSamples));
        }
    }

    private static String getTimeFrameSQL(String table) {
        return "select count(TS), max(TS), min(TS) from " + table + " where TS>=? and TS<=?";
    }

    private synchronized Range getTimeframe(String table, Calendar cFrom, Calendar cTo) {
        try (DBHelper h = new DBHelper(true)) {
            String sql = getTimeFrameSQL(table);
            try (PreparedStatement stm = h.getConnection().prepareStatement(sql)) {
                stm.setTimestamp(1, new java.sql.Timestamp(cFrom.getTimeInMillis()));
                stm.setTimestamp(2, new java.sql.Timestamp(cTo.getTimeInMillis()));
                try (ResultSet rs = stm.executeQuery()) {
                    if (rs.next()) {
                        long count = rs.getLong(1);
                        Timestamp tMax = rs.getTimestamp(2);
                        Timestamp tMin = rs.getTimestamp(3);
                        if (tMax != null && tMin != null) {
                            return new Range(tMax, tMin, count);
                        }
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            ServerLog.getLogger().error("Cannot create time range for {" + table + "} because connection is not established!", e);
        }
        return null;
    }

}