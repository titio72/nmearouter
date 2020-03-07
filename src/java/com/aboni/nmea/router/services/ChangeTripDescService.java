package com.aboni.nmea.router.services;

import com.aboni.nmea.router.track.TripManager;
import com.aboni.utils.db.DBHelper;
import org.json.JSONObject;

public class ChangeTripDescService extends JSONWebService {

    private final TripManager manager;

    public ChangeTripDescService(TripManager manager) {
        super();
        this.manager = manager;
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) throws JSONGenerationException {
        int trip = config.getInteger("trip", -1);
        if (trip != -1) {
            String desc = config.getParameter("desc", "Unknown");
            try (DBHelper db = new DBHelper(true)) {
                if (manager.setDescription(trip, desc))
                    return getOk("Trip description updated!");
                else
                    return getError("No trip matching the id was found!");
            } catch (Exception e) {
                throw new JSONGenerationException(e);
            }
		} else {
            return getError("Error changing trip description! Trip id missing" + trip);
        }
	}

}
