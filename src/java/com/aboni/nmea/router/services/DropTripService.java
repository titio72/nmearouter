package com.aboni.nmea.router.services;

import com.aboni.nmea.router.track.TripManagerX;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class DropTripService extends JSONWebService {

    private final TripManagerX q;

    @Inject
    public DropTripService(@NotNull TripManagerX manager) {
        super();
        q = manager;
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) throws JSONGenerationException {
        try {
            int trip = config.getInteger("trip", -1);
            if (trip != -1) {
                q.deleteTrip(trip);
                return getOk();
            } else {
                return getError("Trip not specified");
            }
        } catch (Exception e) {
            throw new JSONGenerationException("Error deleting track date", e);
        }
    }
}
