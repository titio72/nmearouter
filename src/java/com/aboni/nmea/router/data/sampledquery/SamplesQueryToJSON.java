/*
 * Copyright (c) 2020,  Andrea Boni
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

package com.aboni.nmea.router.data.sampledquery;

import com.aboni.nmea.router.data.Sample;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SamplesQueryToJSON {

    private final ResultContext ctx;

    public SamplesQueryToJSON(SampleWriterFactory writerFactory) {
        ctx = new ResultContext(writerFactory);
    }

    public SamplesQueryToJSON() {
        ctx = new ResultContext(null);
    }

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

    public void fillResponse(String type, List<Sample> samples) {
        SampleWriter w = ctx.getWriter(type);
        if (w != null) {
            for (Sample sample : samples) {
                JSONArray a = getOrCreateSamplesArray(type);
                JSONObject[] res = w.getSampleNode(sample);
                if (res != null) {
                    for (JSONObject n : res) {
                        if (n != null) a.put(n);
                    }
                }
            }
        }
    }

    public JSONObject getResult() {
        return ctx.res;
    }

    private JSONArray getOrCreateSamplesArray(String type) {
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
