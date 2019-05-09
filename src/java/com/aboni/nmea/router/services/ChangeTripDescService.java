package com.aboni.nmea.router.services;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ChangeTripDescService extends JSONWebService {

	@Override
	public JSONObject getResult(ServiceConfig config) {
		int trip = config.getInteger("trip", -1);
		JSONObject res = new JSONObject();
		if (trip!=-1) {
			String desc = config.getParameter("desc", "Unknown");
			try (DBHelper db = getDBHelper()) {
				try (PreparedStatement st1 = db.getConnection().prepareStatement("update trip set description=? where id=?")) {
					st1.setString(1, desc);
					st1.setInt(2, trip);
					st1.executeUpdate();
					res.put("message", "Trip description succesfully changed!");
				}
			} catch (ClassNotFoundException | SQLException e) {
				ServerLog.getLogger().error("Error changing trip description!", e);
				res.put("error", "Error changing trip description! Check log files.");
			}
		} else {
			res.put("error", "Error changing trip description! Unknown trip " + trip);
		}
		return res;
	}

}
