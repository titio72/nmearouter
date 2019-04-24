package com.aboni.nmea.router.services;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class CruisingDaysService extends JSONWebService {
	
	class Trip {
		final int tripId;
		final Set<Date> dates = new TreeSet<>();
		final String desc;
		Date min;
		Date max;

		Trip(int id, String desc) {
			this.desc = desc;
			this.tripId = id;
		}

		void addDate(Date d) {
			if (max==null || max.compareTo(d)<0) max = d;
			if (min==null || min.compareTo(d)>0) min = d;
			dates.add(d);
		}
	}
	
	private static final DateFormat df = new SimpleDateFormat("yyyyMMdd");
	private static final DateFormat short_df = new SimpleDateFormat("dd/MM");

	public CruisingDaysService() {
	}

	private void addToTrip(Map<Integer, Trip> trips, Date d, Integer id, String desc) {
		Trip t = trips.getOrDefault(id, null);
		if (t==null) {
			t = new Trip(id, desc);
			trips.put(id, t);
		}
		t.addDate(d);
	}

	private List<Trip> sortIt(Map<Integer, Trip> trips) {
		List<Trip> triplist = new ArrayList<>(trips.values());
		triplist.sort((o1, o2) -> -o1.min.compareTo(o2.min));
		return triplist;
	}
	
	@Override
	public JSONObject getResult(ServiceConfig config, DBHelper db) {
		try {
			return getJsonObject(getTrips(db));
		} catch (SQLException e) {
			ServerLog.getLogger().Error("Error reading trip list", e);
			JSONObject res = new JSONObject();
			res.put("error", "Error reading trip list. Check the logs.");
			return res;
		}
	}

	private List<Trip> getTrips(DBHelper db) throws SQLException {
		int counter = 0;
		Map<Integer, Trip> tripsDates = new TreeMap<>();
		PreparedStatement stm = db.getConnection().prepareStatement("select track.tripid, Date(track.TS), (select trip.description from trip where trip.id=track.tripid) as description from track group by track.tripid, Date(track.TS)");
		ResultSet rs = stm.executeQuery();
		while (rs.next()) {
			Date d = rs.getDate(2);
			int i = rs.getInt(1);
			String desc = rs.getString(3);
			if (i==0) {
				counter--;
			}
			addToTrip(tripsDates, d, (i==0)?counter:i, desc==null?"":desc);
		}
		rs.close();
		stm.close();
		return sortIt(tripsDates);
	}

	private JSONObject getJsonObject(List<Trip> triplist) {
		JSONObject res = new JSONObject();
		JSONArray trips = new JSONArray();
		for (Trip t: triplist) {
			JSONObject trip = new JSONObject();
			trip.put("trip", t.tripId);
			trip.put("tripLabel", t.desc);
			trip.put("firstDay", df.format(t.min));
			trip.put("lastDay", df.format(t.max));
			JSONArray dates = new JSONArray();
			for (Date d: t.dates) {
        JSONObject dt = new JSONObject();
        String longDate = DateFormat.getDateInstance(DateFormat.SHORT).format(d);
				dt.put("day", longDate);
				dt.put("dayShort", (dates.length()>=1)?short_df.format(d):longDate);
				dt.put("ref", df.format(d));
				dates.put(dt);
			}
			trip.put("dates", dates);
			trips.put(trip);
		}
		res.put("trips", trips);
		return res;
	}
}
