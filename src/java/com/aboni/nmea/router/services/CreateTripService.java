package com.aboni.nmea.router.services;

import com.aboni.utils.db.DBHelper;

import java.sql.*;
import java.util.Calendar;

public class CreateTripService implements WebService {

	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
		String strip = config.getParameter("trip");
		Calendar date = config.getParamAsCalendar(config, "date", null, "yyyyMMdd");
		if (date!=null) {
			int trip;
			try {
				if (strip==null || strip.isEmpty() || strip.trim().charAt(0)=='-') {
					trip = createTrip();
					addToTrip(trip, date);
				} else {
					trip = Integer.parseInt(strip);
					addToTrip(trip, date);
					date.add(Calendar.HOUR, 25); // 25 so it adjusts for DST
					date.set(Calendar.HOUR, 0);
					addToTrip(trip, date);
				}
			} catch (ClassNotFoundException | SQLException e) {
				// TODO
			}
		}
		
	}

	private int createTrip() throws SQLException, ClassNotFoundException {
		DBHelper h = new DBHelper(true);
		Statement st = h.getConnection().createStatement();
		ResultSet rs = st.executeQuery("select max(id) from trip");
		int i = 1;
		if (rs.next()) {
			i = rs.getInt(1) + 1;
		}
		PreparedStatement st1 = h.getConnection().prepareStatement("insert into trip (id, description) values (?, ?)");
		st1.setInt(1, i);
		st1.setString(2, "Trip " + i);
		st1.executeUpdate();
		return i;
	}

	private void addToTrip(int trip, Calendar date) throws ClassNotFoundException, SQLException {
		DBHelper h = new DBHelper(true);
		PreparedStatement stm = h.getConnection().prepareStatement("update track set tripid=? where Date(TS)=?");
		stm.setInt(1, trip);
		stm.setTimestamp(2, new Timestamp(date.getTimeInMillis()));
		stm.executeUpdate();
	}
	
}
