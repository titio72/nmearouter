package com.aboni.nmea.router.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;
import org.json.JSONArray;
import org.json.JSONObject;

public class SpeedAnalyticsService extends JSONWebService {

	private static final String sql = "select sum(dTime), sum(dist) from track where TS>=? and TS<? and speed>=? and speed<?";
	private static final double SPEED_BUCKET = 0.5;
	private static final double SPEED_MIN =  0.0;
	private static final double SPEED_MAX = 12.0;

	private class Stat {
		long time = 0L;
		double distance = 0.0;
	}

	public SpeedAnalyticsService() {
	}

	@Override
	public JSONObject getResult(ServiceConfig config, final DBHelper db) {
        try {
			Map<Double, Stat> distr = new TreeMap<>();

	        DateRangeParameter fromTo = new DateRangeParameter(config);
	        Calendar cFrom = fromTo.getFrom();
	        Calendar cTo = fromTo.getTo();
            
            PreparedStatement stm = db.getConnection().prepareStatement(sql);
            stm.setTimestamp(1, new java.sql.Timestamp(cFrom.getTimeInMillis() ));
			stm.setTimestamp(2, new java.sql.Timestamp(cTo.getTimeInMillis() ));
			
			for (double speed=SPEED_MIN; (speed+SPEED_BUCKET/10.0)<SPEED_MAX; speed+=SPEED_BUCKET) {
	            stm.setDouble(3, speed);
	            stm.setDouble(4, speed + SPEED_BUCKET);
				ResultSet rs = stm.executeQuery();
				Stat s = new Stat();
				if (rs.next()) {
					long t = rs.getLong(1);
					s.distance = rs.getDouble(2);
					s.time = t;
				}
				distr.put(speed, s);
			}
			JSONObject res = new JSONObject();
			JSONArray serie = new JSONArray();
			for (Map.Entry<Double, Stat> entry: distr.entrySet()) {
				JSONObject e = new JSONObject();
				e.put("speed", entry.getKey());
				e.put("time", entry.getValue().time);
				e.put("distance", entry.getValue().distance);
				serie.put(e);
			}
			res.put("serie", serie);
			return res;
		} catch (SQLException e) {
			ServerLog.getLogger().Error("Error reading speeds serie", e);
			JSONObject res = new JSONObject();
			res.put("error", "Error reading speed serie. Check the logs.");
			return res;
        }
	}

	
}
