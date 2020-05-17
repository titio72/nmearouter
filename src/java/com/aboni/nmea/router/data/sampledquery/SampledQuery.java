package com.aboni.nmea.router.data.sampledquery;

import com.aboni.utils.Query;
import com.aboni.utils.ServerLog;
import com.aboni.utils.TimeSeries;
import com.aboni.utils.TimeSeriesSample;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampledQuery {

    private SampledQueryConf conf;
    private SampleWriterFactory sampleWriterFactory;
    private @Inject
    RangeFinder rangeFinder;
    private @Inject
    TimeSeriesReader reader;

    private static class ResultContext {
        final JSONObject res = new JSONObject();
        final Map<String, SampleWriter> sampleWriters = new HashMap<>();
        final SampleWriter defaultWriter = new DefaultSampleWriter();
        final SampleWriterFactory writerFactory;

        public ResultContext(SampleWriterFactory sampleWriterFactory) {
            this.writerFactory = sampleWriterFactory;
        }

        SampleWriter getWriter(String type) {
            if (writerFactory == null)
                return defaultWriter;
            else {
                SampleWriter w = sampleWriters.getOrDefault(type, null);
                if (w == null) {
                    w = writerFactory.getWriter(type);
                    sampleWriters.put(type, w);
                }
                return w;
            }
        }
    }

    @Inject
    public SampledQuery() {
        // do nothing
    }

    public void init(SampledQueryConf conf, SampleWriterFactory sampleWriterFactory) {
        this.conf = conf;
        this.sampleWriterFactory = sampleWriterFactory;
    }

    public JSONObject execute(Query q, int maxSamples) {
        if (conf != null) {
            Range range = rangeFinder.getRange(conf.getTable(), q);
            if (range != null) {
                ResultContext ctx = new ResultContext(sampleWriterFactory);
                Map<String, TimeSeries> timeSeriesMap = reader.getTimeSeries(conf, maxSamples, range);
                if (timeSeriesMap != null) {
                    for (Map.Entry<String, TimeSeries> e : timeSeriesMap.entrySet()) {
                        fillResponse(ctx, e.getKey(), e.getValue().getSamples());
                    }
                } else {
                    fillResponse(ctx, "nothing", null);
                }
                return ctx.res;
            } else {
                ServerLog.getLogger().error("SampledQuery: Could not extract valid range!");
                return null;
            }
        } else {
            ServerLog.getLogger().error("SampledQuery: not initialized!");
            return null;
        }
    }

    private static void fillResponse(ResultContext ctx, String type, List<TimeSeriesSample> samples) {
        SampleWriter w = ctx.getWriter(type);
        if (w != null) {
            for (TimeSeriesSample sample : samples) {
                JSONArray a = getOrCreateSamplesArray(ctx, type);
                JSONObject[] res = w.getSampleNode(sample);
                if (res != null) {
                    for (JSONObject n : res) {
                        if (n != null) a.put(n);
                    }
                }
            }
        }
    }

    private static JSONArray getOrCreateSamplesArray(ResultContext ctx, String type) {
        JSONArray a;
        if (ctx.res.has(type)) {
            a = ctx.res.getJSONArray(type);
        } else {
            a = new JSONArray();
            ctx.res.put(type, a);
        }
        return a;
    }
}
