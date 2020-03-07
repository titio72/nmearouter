package com.aboni.nmea.router.services;

import com.aboni.nmea.router.track.TrackManagementException;
import com.aboni.nmea.router.track.TrackQueryManager;
import com.aboni.utils.ServerLog;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CruisingDaysService extends JSONWebService {

    private final DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
    private final DateFormat shortDateFormatter = new SimpleDateFormat("dd/MM");

    private final TrackQueryManager manager;

    public CruisingDaysService(TrackQueryManager q) {
        super();
        manager = q;
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) {
        try {
            return getJsonObject(manager.getTrips());
        } catch (TrackManagementException e) {
            ServerLog.getLogger().error("Error reading trip list", e);
            JSONObject res = new JSONObject();
            res.put("error", "Error reading trip list. Check the logs.");
            return res;
        }
    }

    private JSONObject getJsonObject(List<TrackQueryManager.Trip> tripList) {
        JSONObject res = new JSONObject();
        JSONArray trips = new JSONArray();
        for (TrackQueryManager.Trip t : tripList) {
            JSONObject trip = new JSONObject();
            trip.put("trip", t.getTrip());
            trip.put("tripLabel", t.getTripDescription());
            trip.put("firstDay", dateFormatter.format(t.getMinDate()));
            trip.put("lastDay", dateFormatter.format(t.getMaxDate()));
            JSONArray dates = new JSONArray();
            for (Date d : t.getDates()) {
                JSONObject dt = new JSONObject();
                String longDate = DateFormat.getDateInstance(DateFormat.SHORT).format(d);
                dt.put("day", longDate);
                dt.put("dayShort", (dates.length() >= 1) ? shortDateFormatter.format(d) : longDate);
                dt.put("ref", dateFormatter.format(d));
                dates.put(dt);
            }
            trip.put("dates", dates);
            trips.put(trip);
        }
		res.put("trips", trips);
		return res;
	}
}
