package com.aboni.nmea.router.data.meteo;

import org.json.JSONObject;

import java.time.Instant;

public interface WindStatsReader {

    JSONObject getWindStats(Instant from, Instant to, int sectors) throws MeteoManagementException;

}
