package com.aboni.nmea.router.services;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

public class SpeedAnalyticsService extends JSONWebService {

	private static final String SQL = "select round(speed*2)/2, sum(dTime), sum(dist) from track where TS>=? and TS<? and anchor=0 group by round(speed*2)/2";
	private static final double SPEED_BUCKET = 0.5;
	private static final double SPEED_MIN =  0.0;
	private static final double SPEED_MAX = 12.0;

	private static class Stat {
		long time = 0L;
		double distance = 0.0;
	}

	public SpeedAnalyticsService() {
        super();
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) {
		Map<Double, Stat> distr = new TreeMap<>();
		for (double speed = SPEED_MIN; (speed + SPEED_BUCKET / 10.0) < SPEED_MAX; speed += SPEED_BUCKET) {
			distr.put(speed, new Stat());
		}

		DateRangeParameter fromTo = new DateRangeParameter(config);
		Calendar cFrom = fromTo.getFrom();
		Calendar cTo = fromTo.getTo();

        try (DBHelper db = new DBHelper(true)) {
			try (PreparedStatement stm = db.getConnection().prepareStatement(SQL)) {
				stm.setTimestamp(1, new java.sql.Timestamp(cFrom.getTimeInMillis()));
				stm.setTimestamp(2, new java.sql.Timestamp(cTo.getTimeInMillis()));
				try (ResultSet rs = stm.executeQuery()) {
					while (rs.next()) {
						double s = rs.getDouble(1);
						distr.get(s).distance = rs.getDouble(3);
						distr.get(s).time = rs.getInt(2);
					}
				}

				JSONObject res = new JSONObject();
				JSONArray serie = new JSONArray();
				for (Map.Entry<Double, Stat> entry : distr.entrySet()) {
					JSONObject e = new JSONObject();
					e.put("speed", entry.getKey());
					e.put("time", entry.getValue().time);
					e.put("distance", entry.getValue().distance);
					serie.put(e);
				}
				res.put("serie", serie);
				return res;
			}
		} catch (SQLException | ClassNotFoundException e) {
			ServerLog.getLogger().error("Error reading speeds serie", e);
			JSONObject res = new JSONObject();
			res.put("error", "Error reading speed serie. Check the logs.");
			return res;
        }
	}
}
