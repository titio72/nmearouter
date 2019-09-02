package com.aboni.nmea.router.services;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DistanceAnalyticsService extends JSONWebService {

	private static final String SQL = "select year(TS), month(TS), sum(dist*(1-anchor) 	), sum(dTime*(1-anchor)), count(distinct day(TS)) from track group by year(TS), month(TS)";

	public DistanceAnalyticsService() {
		super();
		setLoader(this::getResult);
	}

	private JSONObject getResult(ServiceConfig config) {
		try (DBHelper db = new DBHelper(true)) {
			try (PreparedStatement stm = db.getConnection().prepareStatement(SQL)) {
				JSONObject res = new JSONObject();
				JSONArray samples = new JSONArray();
				try (ResultSet rs = stm.executeQuery()) {
					int lastM = 0;
					int lastY = 0;
					while (rs.next()) {
						int y = rs.getInt(1);
						if (lastY<y && lastY>0) {
							for (int i = lastM + 1; i<=12; i++) {
								JSONArray e = new JSONArray(new Object[]{lastY, i, 0.0, 0, 0});
								samples.put(e);
							}
							lastM = 0;
						}
						lastY = y;

						int m = rs.getInt(2);
						if ((m - lastM)>1) {
							for (int i = lastM + 1; i<m; i++) {
								JSONArray e = new JSONArray(new Object[]{y, i, 0.0, 0, 0});
								samples.put(e);
							}
						}
						lastM = m;
						double dist = rs.getDouble(3);
						double sailTime = rs.getDouble(4);
						double days = rs.getDouble(5);
						JSONArray e = new JSONArray(new Object[]{y, m, dist, sailTime / 3600, days});
						samples.put(e);
					}
				}
				res.put("NM_per_month", samples);
				return res;
			}
		} catch (SQLException | ClassNotFoundException e) {
			ServerLog.getLogger().error("Error reading distance stats", e);
			JSONObject res = new JSONObject();
			res.put("error", "Error reading distance stats. Check the logs.");
			return res;
        }
	}
}
