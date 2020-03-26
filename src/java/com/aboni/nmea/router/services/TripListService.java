package com.aboni.nmea.router.services;

import com.aboni.nmea.router.data.track.Trip;
import com.aboni.nmea.router.data.track.TripManagerX;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.List;

public class TripListService extends JSONWebService {

    private final TripManagerX manager;

    @Inject
    public TripListService(@NotNull TripManagerX manager) {
        super();
        this.manager = manager;
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) throws JSONGenerationException {
        int year = config.getInteger("year", Calendar.getInstance().get(Calendar.YEAR));
        try {
            List<Trip> trips = (year == 0) ? manager.getTrips(true) : manager.getTrips(year, true);
            return new TripsToJSON(trips).go();
        } catch (Exception e) {
            throw new JSONGenerationException(e);
        }
    }
}
