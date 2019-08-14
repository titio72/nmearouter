package com.aboni.nmea.router.services;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;
import org.json.JSONObject;

import java.sql.*;
import java.util.Calendar;

public class CreateTripService extends JSONWebService {

    public CreateTripService() {
        super();
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) {
		String strip = config.getParameter("trip");
		Calendar date = config.getParamAsCalendar(config, "date", null, "yyyyMMdd");
		if (date!=null) {
			int trip;
            try (DBHelper h = new DBHelper(true)) {
				if (strip==null || strip.isEmpty() || strip.trim().charAt(0)=='-') {
					trip = createTrip(h);
					addToTrip(trip, date, h);
				} else {
					trip = Integer.parseInt(strip);
					addToTrip(trip, date, h);
					date.add(Calendar.HOUR, 25); // 25 so it adjusts for DST
					date.set(Calendar.HOUR, 0);
					addToTrip(trip, date, h);
				}
				return getOk();
			} catch (SQLException | ClassNotFoundException e) {
				String msg = "Error creating or adding days to a trip {" + strip + "} {" + date + "}";
				ServerLog.getLogger().error(msg, e);
				JSONObject res = new JSONObject();
				res.put("Error", msg);
				return res;
			}
		} else {
			String msg = "No valid date selected to create or add days to a trip {" + strip + "}";
			ServerLog.getLogger().error(msg);
			JSONObject res = new JSONObject();
			res.put("Error", msg);
			return res;
		}

	}

	private int createTrip(DBHelper h) throws SQLException {
		int i;
		try (Statement st = h.getConnection().createStatement()) {
			try (ResultSet rs = st.executeQuery("select max(id) from trip")) {
				if (rs.next()) {
					i = rs.getInt(1) + 1;
				} else {
					i = 1;
				}
			}
			try (PreparedStatement st1 = h.getConnection().prepareStatement("insert into trip (id, description) values (?, ?)")) {
				st1.setInt(1, i);
				st1.setString(2, "Trip " + i);
				st1.executeUpdate();
			}
		}
		return i;
	}

	private void addToTrip(int trip, Calendar date, DBHelper h) throws SQLException {
		try (PreparedStatement stm = h.getConnection().prepareStatement("update track set tripid=? where Date(TS)=?")) {
			stm.setInt(1, trip);
			stm.setTimestamp(2, new Timestamp(date.getTimeInMillis()));
			stm.executeUpdate();
		}
	}
	
}
