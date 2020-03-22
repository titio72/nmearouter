package com.aboni.nmea.router.meteo;

import org.json.JSONObject;

import java.time.Instant;

public interface Meteo {

    JSONObject getMeteoSeries(Instant from, Instant to) throws MeteoManagementException;

}
