package com.aboni.nmea.router.data.meteo;

import org.json.JSONObject;

import java.time.Instant;

public interface Meteo {

    JSONObject getMeteoSeries(Instant from, Instant to) throws MeteoManagementException;

}
