package com.aboni.nmea.router.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.aboni.utils.db.DBHelper;

public class SpeedAnalyticsService implements WebService {

	private static final String APPLICATION_JSON = "application/json";
	private DBHelper db;
	
	public SpeedAnalyticsService() {
	}

	private double SPEED_BUCKET = 0.5;
	private double SPEED_MIN =  0.0;
	private double SPEED_MAX = 12.0;
	
	private class Stat {
		long time = 0L;
		double distance = 0.0;
	}
	
	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {

		Map<Double, Stat> distr = new TreeMap<>();
		String sql = "select sum(dTime), sum(dist) from track where tripid=? and speed>=? and speed<?";
        try {
			response.setContentType(APPLICATION_JSON);

			db = new DBHelper(true);
            
            int trip = Integer.parseInt(config.getParameter("trip"));
            
            PreparedStatement stm = db.getConnection().prepareStatement(sql);
            stm.setInt(1, trip);
			for (double speed=SPEED_MIN; (speed+SPEED_BUCKET/10.0)<SPEED_MAX; speed+=SPEED_BUCKET) {
	            stm.setDouble(2, speed);
	            stm.setDouble(3, speed + SPEED_BUCKET);
				ResultSet rs = stm.executeQuery();
				Stat s = new Stat();
				if (rs.next()) {
					long t = rs.getLong(1);
					double d = rs.getDouble(2);
					s.distance = d;
					s.time = t;
				}
				distr.put(speed, s);
			}

			Iterator<Map.Entry<Double, Stat>> i = distr.entrySet().iterator();

            boolean first = true;
            response.getWriter().write("{\"serie\":[");
	        while (i.hasNext()) {
		        if (!first) {
	                response.getWriter().write(",");
	        	}
				Map.Entry<Double, Stat> e = i.next();
	            response.getWriter().write("{\"speed\":" + e.getKey() + ",");
	            response.getWriter().write("\"time\":" + e.getValue().time + ",");
	            response.getWriter().write("\"distance\":" + e.getValue().distance + "}");
	            first = false;
	        }
            response.getWriter().write("]}");
           
            
		} catch (Exception e) {
            response.setContentType("text/html;charset=utf-8");
            try { e.printStackTrace(response.getWriter()); } catch (Exception ee) {}
            response.error(e.getMessage());			
		} finally {
			try {
				db.close();
			} catch (Exception e2) {}
		}
		
	}

	
}
