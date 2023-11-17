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

import com.aboni.nmea.router.data.Query;
import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.data.sampledquery.SampledQuery;
import com.aboni.nmea.router.data.sampledquery.SampledQueryConf;
import com.aboni.nmea.router.data.sampledquery.SampledQueryException;
import com.aboni.log.Log;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.aboni.log.LogStringBuilder;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Named;

public class MeteoService extends JSONWebService {

    private static final int DEFAULT_MAX_SAMPLES = 500;

    private final SampledQueryConf conf;
    private final QueryFactory queryFactory;
    private SampledQuery sampledQuery;

    @Inject
    public MeteoService(Log log, QueryFactory queryFactory, @Named(Constants.TAG_METEO) SampledQueryConf conf) {
        super(log);
        if (queryFactory==null) throw new IllegalArgumentException("Query factory is null");
        if (conf==null) throw new IllegalArgumentException("Sampled query configuration is null");
        this.conf = conf;
        this.queryFactory = queryFactory;
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) {
        Query q = queryFactory.getQuery(config);
        if (q != null) {
            SampledQuery sq = getSampledQuery();
            try {
                return sq.execute(q, config.getInteger("samples", DEFAULT_MAX_SAMPLES));
            } catch (SampledQueryException e) {
                getLogger().errorForceStacktrace(() -> LogStringBuilder.start("MeteoService").wO("execute").wV("query", q).toString(), e);
                return getError("Error executing query");
            }
        } else {
            return getError("No valid query specified!");
        }
    }

    private SampledQuery getSampledQuery() {
        if (sampledQuery == null) {
            sampledQuery = ThingsFactory.getInstance(SampledQuery.class);
            sampledQuery.init(conf, null);
        }
        return sampledQuery;
    }
}
