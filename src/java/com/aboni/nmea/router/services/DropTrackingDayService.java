package com.aboni.nmea.router.services;

import com.aboni.nmea.router.track.TrackQueryManager;
import org.json.JSONObject;

import javax.inject.Inject;
import java.time.LocalDate;

public class DropTrackingDayService extends JSONWebService {

    private final TrackQueryManager q;

    @Inject
    public DropTrackingDayService(TrackQueryManager manager) {
        super();
        q = manager;
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) throws JSONGenerationException {
        try {
            LocalDate cDate = config.getParamAsDate("date", null);
            if (cDate != null) {
                q.dropDay(cDate);
                return getOk("Date deleted");
            } else {
                return getOk("No date to delete");
            }
        } catch (Exception e) {
            throw new JSONGenerationException("Error deleting track date", e);
        }
    }
}
