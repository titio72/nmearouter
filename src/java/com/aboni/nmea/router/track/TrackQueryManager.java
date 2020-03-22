package com.aboni.nmea.router.track;

import org.json.JSONObject;

public interface TrackQueryManager {

    JSONObject getYearlyStats() throws TrackManagementException;

}
