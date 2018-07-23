package com.aboni.nmea.router.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;

import com.aboni.utils.db.DBHelper;

public class TripInfoService implements WebService {

	private static final String APPLICATION_JSON = "application/json";
	private DBHelper db;
	
	public TripInfoService() {
	}

	private double SPEED_THRESHOLD = 0.3;
	
	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
		String sql = "select sum(dist), max(speed), min(TS), max(TS), max(maxSpeed) from track where tripid=?";
		String sql1 = "select sum(dTime) from track where tripid=? and anchor=0 and speed>" + SPEED_THRESHOLD;
        response.setContentType(APPLICATION_JSON);
    	
        double dist = 0;
    	double maxSpeed30 = 0;
    	double maxSpeed = 0;
    	Timestamp start = null;
    	Timestamp end = null;
    	long sailTime = 0;
        
        
        try {
            db = new DBHelper(true);
            
            int trip = Integer.parseInt(config.getParameter("trip"));
            
            PreparedStatement stm = db.getConnection().prepareStatement(sql);
            stm.setInt(1, trip);
            
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
            	dist = rs.getDouble(1);
            	maxSpeed30 = rs.getDouble(2);
            	maxSpeed = rs.getDouble(5);
            	start = rs.getTimestamp(3);
            	end = rs.getTimestamp(4);
            }            

            PreparedStatement stm1 = db.getConnection().prepareStatement(sql1);
            stm1.setInt(1, trip);

            ResultSet rs1 = stm1.executeQuery();
            if (rs1.next()) {
            	sailTime = rs1.getLong(1);
            }            
            
        	response.getWriter().append("{");
        	response.getWriter().append("\"dist\":" + dist);
        	response.getWriter().append(",");
        	response.getWriter().append("\"maxspeed30\":" + maxSpeed30);
        	response.getWriter().append(",");
        	response.getWriter().append("\"maxspeed\":" + maxSpeed);
        	response.getWriter().append(",");
        	response.getWriter().append("\"sailtime\":" + sailTime);
        	response.getWriter().append(",");
        	if (start!=null && end!=null) {
	        	response.getWriter().append("\"start\":\"" + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(start) + "\"");
	        	response.getWriter().append(",");
	        	response.getWriter().append("\"end\":\"" + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(end) + "\"");
	        	response.getWriter().append(",");
	        	response.getWriter().append("\"duration\":" + (end.getTime()-start.getTime())/1000);
        	}
        	response.getWriter().append("}");
        	response.ok();
            
            
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
