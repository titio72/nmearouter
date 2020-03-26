package com.aboni.nmea.router.services.impl;

import com.aboni.nmea.router.services.QueryFactory;
import com.aboni.nmea.router.services.ServiceConfig;
import com.aboni.utils.Query;
import com.aboni.utils.QueryByDate;
import com.aboni.utils.QueryById;

import javax.inject.Inject;
import java.time.Instant;

public class QueryFactoryImpl implements QueryFactory {

    @Inject
    public QueryFactoryImpl() {
        // do nothing
    }

    @Override
    public Query getQuery(ServiceConfig config) {
        final int trip = config.getInteger("trip", -1);
        if (trip != -1) {
            return new QueryById(trip);
        } else {
            Instant cFrom = config.getParamAsInstant("from", Instant.now().minusSeconds(86400), 0);
            Instant cTo = config.getParamAsInstant("to", cFrom.plusSeconds(86401), 0);
            return new QueryByDate(cFrom, cTo);
        }
    }
}
