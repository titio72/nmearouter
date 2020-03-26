package com.aboni.nmea.router.services;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.data.sampledquery.SampleWriter;
import com.aboni.nmea.router.data.sampledquery.SampleWriterFactory;
import com.aboni.nmea.router.data.sampledquery.SampledQuery;
import com.aboni.nmea.router.data.sampledquery.SampledQueryConf;
import com.aboni.utils.Query;
import com.aboni.utils.ThingsFactory;
import com.aboni.utils.TimeSeriesSample;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;

public class SpeedService extends JSONWebService {

    private static final int DEFAULT_MAX_SAMPLES = 500;

    private @Inject
    @Named(Constants.TAG_SPEED)
    SampledQueryConf conf;
    private @Inject
    QueryFactory queryFactory;
    private SampledQuery sampledQuery;

    private static class SpeedSampleWriter implements SampleWriter {

        private long lastTS = 0;
        private double lastV = 0.0;
        private boolean lastSkipped = true;
        private boolean lastNull = false;
        private int count = 0;

        @Override
		public JSONObject[] getSampleNode(TimeSeriesSample s) {
			JSONObject[] ret;
			if (s.getValue() <= 0.1 && lastV <= 0.1) {
				if (count > 0) {
					if (!lastSkipped) {
						// speed is 0 but last sample was not skipped so write a 0 to bring chart to 0
						ret = new JSONObject[]{writeZero(s.getT0())};
						lastNull = false;
						count++;
					} else if (!lastNull) {
						// last one was speed=0 but it was written. Write a null.
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
			lastV = s.getValue();
			lastTS = s.getLastTs();
			return ret;
		}

		private JSONObject writeValue(TimeSeriesSample s) {
			return write(s.getT0(), Utils.round(s.getValueMin(), 2), Utils.round(s.getValue(), 2), Utils.round(s.getValueMax(), 2));
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
    public SpeedService() {
        super();
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) {
        Query q = queryFactory.getQuery(config);
        if (q != null) {
            SampledQuery sq = getSampledQuery();
            return sq.execute(q, config.getInteger("samples", DEFAULT_MAX_SAMPLES));
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
