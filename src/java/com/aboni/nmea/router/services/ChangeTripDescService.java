package com.aboni.nmea.router.services;

import com.aboni.nmea.router.data.track.TripManagerException;
import com.aboni.nmea.router.data.track.TripManagerX;
import com.aboni.utils.db.DBHelper;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class ChangeTripDescService extends JSONWebService {

    private final TripManagerX manager;

    @Inject
    public ChangeTripDescService(@NotNull TripManagerX manager) {
        super();
        this.manager = manager;
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) throws JSONGenerationException {
        int trip = config.getInteger("trip", -1);
        if (trip != -1) {
            String desc = config.getParameter("desc", "Unknown");
            try (DBHelper db = new DBHelper(true)) {
                manager.setTripDescription(trip, desc);
                return getOk("Trip description updated!");
            } catch (TripManagerException e) {
                return getOk(e.getMessage());
            } catch (Exception e) {
                throw new JSONGenerationException(e);
            }
		} else {
            return getError("Error changing trip description! Trip id missing" + trip);
        }
	}

}
