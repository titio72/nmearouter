package com.aboni.nmea.router.services;

import org.json.JSONObject;

public interface WebServiceJSONLoader {
    JSONObject getResult(ServiceConfig config) throws JSONGenerationException;
}
