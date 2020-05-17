package com.aboni.nmea.router.data.sampledquery;

import com.aboni.utils.Query;

public interface RangeFinder {
    Range getRange(String table, Query q);
}
