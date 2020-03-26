package com.aboni.nmea.router.services;

import com.aboni.utils.Query;

public interface QueryFactory {
    Query getQuery(ServiceConfig config);
}
