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

package com.aboni.nmea.router.data.sampledquery;

import com.aboni.nmea.router.data.Query;
import com.aboni.nmea.router.data.TimeSeries;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.Map;

public class SampledQuery {

    private SampledQueryConf conf;
    private SampleWriterFactory sampleWriterFactory;
    private final RangeFinder rangeFinder;
    private final TimeSeriesReader reader;

    @Inject
    public SampledQuery(RangeFinder rangeFinder, TimeSeriesReader reader) {
        if (reader==null) throw new IllegalArgumentException("TimeSeriesReader cannot be null");
        if (rangeFinder==null) throw new IllegalArgumentException("RangeFinder cannot be null");
        this.rangeFinder = rangeFinder;
        this.reader = reader;
    }

    public void init(SampledQueryConf conf, SampleWriterFactory sampleWriterFactory) {
        this.conf = conf;
        this.sampleWriterFactory = sampleWriterFactory;
    }

    public JSONObject execute(Query q, int maxSamples) throws SampledQueryException {
        if (conf != null) {
            Range range = rangeFinder.getRange(conf.getTable(), q);
            if (range != null) {
                SamplesQueryToJSON ctx = new SamplesQueryToJSON(sampleWriterFactory);
                Map<String, TimeSeries> timeSeriesMap = reader.getTimeSeries(conf, maxSamples, range);
                if (timeSeriesMap != null) {
                    for (Map.Entry<String, TimeSeries> e : timeSeriesMap.entrySet()) {
                        ctx.fillResponse(e.getKey(), e.getValue().getSamples());
                    }
                } else {
                    ctx.fillResponse("nothing", null);
                }
                return ctx.getResult();
            } else {
                throw new SampledQueryException("Could not extract valid range");
            }
        } else {
            throw new SampledQueryException("Query not initialized");
        }
    }
}
