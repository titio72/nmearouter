package com.aboni.nmea.router.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;

import com.aboni.utils.DBHelper;

public class TripInfoService implements WebService {

	private static final String APPLICATION_JSON = "application/json";
	private DBHelper db;
	
	public TripInfoService() {
	}

	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
		String sql = "select sum(dist), max(speed), min(TS), max(TS), max(maxSpeed) from track where tripid=?";
        response.setContentType(APPLICATION_JSON);
		try {
            db = new DBHelper(true);
            
            int trip = Integer.parseInt(config.getParameter("trip"));
            
            PreparedStatement stm = db.getConnection().prepareStatement(sql);
            stm.setInt(1, trip);
            
            ResultSet rs = stm.executeQuery();

            if (rs.next()) {
            	double dist = rs.getDouble(1);
            	double maxSpeed30 = rs.getDouble(2);
            	double maxSpeed = rs.getDouble(5);
            	Timestamp start = rs.getTimestamp(3);
            	Timestamp end = rs.getTimestamp(4);
            	response.getWriter().append("{");
            	response.getWriter().append("\"dist\":" + dist);
            	response.getWriter().append(",");
            	response.getWriter().append("\"maxspeed30\":" + maxSpeed30);
            	response.getWriter().append(",");
            	response.getWriter().append("\"maxspeed\":" + maxSpeed);
            	response.getWriter().append(",");
            	response.getWriter().append("\"start\":\"" + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(start) + "\"");
            	response.getWriter().append(",");
            	response.getWriter().append("\"end\":\"" + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(end) + "\"");
            	response.getWriter().append(",");
            	response.getWriter().append("\"duration\":" + (end.getTime()-start.getTime())/1000);
            	response.getWriter().append("}");
            	response.ok();
            }            

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
