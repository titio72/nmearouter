package com.aboni.nmea.router.services;

import com.aboni.nmea.router.track.TrackQueryManager;
import org.json.JSONObject;

import java.util.Calendar;

public class DropTrackingDayService extends JSONWebService {

    private final TrackQueryManager q;

    public DropTrackingDayService(TrackQueryManager manager) {
        super();
        q = manager;
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) throws JSONGenerationException {
        try {
            Calendar cDate = config.getParamAsCalendar("date", null, "yyyyMMdd");
            q.dropDay(cDate);
            return getOk("Date deleted");
        } catch (Exception e) {
            throw new JSONGenerationException("Error deleting track date", e);
        }
    }
}
