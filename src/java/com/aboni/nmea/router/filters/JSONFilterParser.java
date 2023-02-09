package com.aboni.nmea.router.filters;

import org.json.JSONObject;

public interface JSONFilterParser {
    NMEAFilter getFilter(JSONObject obj);
}
