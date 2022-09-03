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

package com.aboni.nmea.router.services;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.data.StatsSample;
import com.aboni.nmea.router.data.sampledquery.*;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import com.aboni.utils.Query;
import com.aboni.utils.ThingsFactory;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

public class SpeedService extends JSONWebService {

    private static final int DEFAULT_MAX_SAMPLES = 500;

    private final SampledQueryConf conf;
    private final QueryFactory queryFactory;
    private SampledQuery sampledQuery;

    private final Log log;

    private static class SpeedSampleWriter implements SampleWriter {

        private long lastTS = 0;
        private double lastV = 0.0;
        private boolean lastSkipped = true;
        private boolean lastNull = false;
        private int count = 0;

        @Override
        public JSONObject[] getSampleNode(StatsSample s) {
            JSONObject[] ret;
            if (s.getAvg() <= 0.1 && lastV <= 0.1) {
                if (count > 0) {
                    if (!lastSkipped) {
                        // speed is 0 but last sample was not skipped so write a 0 to bring chart to 0
                        ret = new JSONObject[]{writeZero(s.getT0())};
                        lastNull = false;
                        count++;
                    } else if (!lastNull) {
                        // last one was speed=0, but it was written. Write a null.
                        ret = new JSONObject[]{writeNull(s.getT0())};
                        lastNull = true;
                        count++;
                    } else {
                        // last one was skipped and speed is still 0 - skip again
                        ret = null;
                    }
                } else {
                    // skip
                    ret = null;
                }
                lastSkipped = true;
            } else {
                if (lastSkipped && count > 0) {
                    ret = new JSONObject[]{writeNull(lastTS - 1), writeZero(lastTS), writeValue(s)};
                    count += 2;
                } else {
                    ret = new JSONObject[]{writeValue(s)};
                    count++;
                }
                lastSkipped = false;
                lastNull = false;
            }
            lastV = s.getAvg();
            lastTS = s.getT1();
            return ret;
        }

        private JSONObject writeValue(StatsSample s) {
            return write(s.getT0(),
                    Utils.round(s.getMin(), 2),
                    Utils.round(s.getAvg(), 2),
                    Utils.round(s.getMax(), 2));
        }

        private JSONObject writeNull(long ts) {
            return write(ts, "null", "null", "null");
        }

        private JSONObject writeZero(long ts) {
            return write(ts, "0.0", "0.0", "0.0");
        }

        private JSONObject write(long ts, Object min, Object avg, Object max) {
            JSONObject s = new JSONObject();
            s.put("time", ts);
            s.put("vMin", min);
            s.put("v", avg);
            s.put("vMax", max);
            return s;
        }
    }

    private static class SpeedSampleWriterFactory implements SampleWriterFactory {

        @Override
        public SampleWriter getWriter(String type) {
            return new SpeedSampleWriter();
        }
    }

    @Inject
    public SpeedService(@NotNull Log log, @NotNull QueryFactory queryFactory, @Named(Constants.TAG_SPEED) SampledQueryConf conf) {
        super(log);
        this.log = log;
        this.queryFactory = queryFactory;
        this.conf = conf;
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) {
        Query q = queryFactory.getQuery(config);
        if (q != null) {
            SampledQuery sq = getSampledQuery();
            try {
                return sq.execute(q, config.getInteger("samples", DEFAULT_MAX_SAMPLES));
            } catch (SampledQueryException e) {
                LogStringBuilder.start("SpeedService").wO("execute").wV("query", q).errorForceStacktrace(log, e);
                return getError("Error executing query");
            }
        } else {
            return getError("No valid query specified!");
        }
    }

    private SampledQuery getSampledQuery() {
        if (sampledQuery == null) {
            sampledQuery = ThingsFactory.getInstance(SampledQuery.class);
            sampledQuery.init(conf, new SpeedSampleWriterFactory());
        }
        return sampledQuery;
    }
}
