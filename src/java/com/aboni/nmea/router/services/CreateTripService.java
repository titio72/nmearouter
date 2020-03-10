package com.aboni.nmea.router.services;

import com.aboni.nmea.router.track.TripManager;
import com.aboni.utils.ServerLog;
import org.json.JSONObject;

import javax.inject.Inject;
import java.time.LocalDate;

public class CreateTripService extends JSONWebService {

    private final TripManager manager;

    @Inject
    public CreateTripService(TripManager manager) {
        super();
        this.manager = manager;
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) throws JSONGenerationException {
        String strip = config.getParameter("trip");
        LocalDate date = config.getParamAsDate("date", null);
        if (date != null) {
            try {
                if (strip == null || strip.isEmpty() || strip.trim().charAt(0) == '-') {
                    int i = manager.createTrip();
                    manager.addDateToTrip(i, date);
                } else {
                    int i = Integer.parseInt(strip);
                    // add the adjacent following day
                    date = date.plusDays(1);
                    manager.addDateToTrip(i, date);
                }
                return getOk();
            } catch (Exception e) {
                throw new JSONGenerationException(e);
            }
        } else {
            String msg = "No valid date selected to create or add days to a trip {" + strip + "}";
            ServerLog.getLogger().error(msg);
            throw new JSONGenerationException(msg);
        }
    }
}
