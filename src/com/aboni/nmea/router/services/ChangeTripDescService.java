package com.aboni.nmea.router.services;

import com.aboni.utils.DBHelper;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChangeTripDescService implements WebService {

	public ChangeTripDescService() {
	}
	
	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
		String strip = config.getParameter("trip");
		String desc = config.getParameter("desc");
		if (strip!=null && desc!=null) {
			int trip;
			try {
				trip = Integer.parseInt(strip);
				setTripName(trip, desc);
			} catch (ClassNotFoundException | SQLException e) {
				// TODO
			}
		}
	}
	
	private void setTripName(int trip, String desc) throws SQLException, ClassNotFoundException {
		DBHelper h = new DBHelper(true);
		PreparedStatement st1 = h.getConnection().prepareStatement("update trip set description=? where id=?");
		st1.setString(1, desc);
		st1.setInt(2, trip);
		st1.executeUpdate();
	}
}
