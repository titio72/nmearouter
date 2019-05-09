package com.aboni.nmea.router.services;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

public class DropTrackingDayService extends JSONWebService {

	@Override
	public JSONObject getResult(ServiceConfig config) {
		try (DBHelper db = getDBHelper()) {
			try (PreparedStatement stm = db.getConnection().prepareStatement("delete from track where Date(TS)=?")) {
				Calendar cDate = config.getParamAsCalendar(config, "date", null, "yyyyMMdd");
				if (cDate != null) {
					stm.setDate(1, new java.sql.Date(cDate.getTimeInMillis()));
					stm.executeUpdate();
				}
				JSONObject res = new JSONObject();
				res.put("msg", "Date succesfully deleted");
				return res;
			}
		} catch (SQLException | ClassNotFoundException e) {
			ServerLog.getLogger().error("Error deleting date", e);
			JSONObject res = new JSONObject();
			res.put("error", "Error deleting date");
			return res;
		}
	}
}
