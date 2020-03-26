package com.aboni.nmea.router.data.track;

import org.json.JSONObject;

public interface TrackQueryManager {

    JSONObject getYearlyStats() throws TrackManagementException;

}
