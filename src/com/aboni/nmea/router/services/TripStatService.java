package com.aboni.nmea.router.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import com.aboni.utils.db.DBHelper;

public class TripStatService implements WebService {
	//2017-10-29T01:08:58Z
	private static DateFormat df = new SimpleDateFormat("dd/HH:mm:ssZ", Locale.US);
	static {
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	private static final String APPLICATION_JSON = "application/json";
	private DBHelper db;
	
	public TripStatService() {
	}

	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
		String sql = "select " 
				+ "tripid, "
				+ "min(description) as description, "
				+ "min(TS) as start, "
				+ "max(TS) as end, "
				+ "timediff(max(TS),min(TS)) as duration, "
				+ "sum(dist) as distance " 
				+ "from track inner join trip on track.tripid = trip.id " 
				+ "where TS>=? and TS<? "
				+ "group by tripid order by start desc";

		response.setContentType(APPLICATION_JSON);
		try {
            db = new DBHelper(true);
            String sYear = config.getParameter("year");
            int year = -1;
            try { year = Integer.parseInt(sYear); } catch (Exception e) {}
            
            Calendar cFrom = getFirstDayOfYear(year);
            Calendar cTo = getFirstDayOfYear(cFrom.get(Calendar.YEAR)+1);

            PreparedStatement stm = db.getConnection().prepareStatement(sql);
            stm.setTimestamp(1, new Timestamp(cFrom.getTimeInMillis()));
            stm.setTimestamp(2, new Timestamp(cTo.getTimeInMillis()));
            
            ResultSet rs = stm.executeQuery();

            
            long totDuration = 0;
            double totalDistance = 0.0;
            		
            
            JSONObject res = new JSONObject();
            JSONArray trips = new JSONArray();
            res.put("trips", trips);
            while (rs.next()) {
            	int tripId = rs.getInt(1);
            	String desc = rs.getString(2);
            	Timestamp start = rs.getTimestamp(3);
            	Timestamp end = rs.getTimestamp(4);
            	long duration = (end.getTime()-start.getTime()) / 1000; totDuration += duration;
            	double distance = rs.getDouble(6); totalDistance += distance;

            	JSONObject trip = new JSONObject();
            	trip.put("id", tripId);
            	trip.put("description", desc);
            	trip.put("start", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(start));
            	trip.put("end", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(end));
            	
            	long days = duration / (60 * 60 * 24);
            	duration = duration % (60 * 60 * 24);
            	if (days==0)
            		trip.put("duration", String.format("%dh %02dm", duration / 3600, (duration % 3600) / 60));
            	else
            		trip.put("duration", String.format("%dd %dh %02dm", days, duration / 3600, (duration % 3600) / 60));
            	trip.put("distance", String.format("%.2f", distance));

            	trips.put(trip);
            }         
        	
            JSONObject tot = new JSONObject();
            tot.put("start", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(cFrom.getTime()));
            tot.put("end", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date()));
            tot.put("distance", String.format("%.2f", totalDistance));
        	long days = totDuration / (60 * 60 * 24);
        	totDuration = totDuration% (60 * 60 * 24);
        	if (days==0)
        		tot.put("duration", String.format("%dh %02dm", totDuration / 3600, (totDuration % 3600) / 60));
        	else
        		tot.put("duration", String.format("%dd %dh %02dm", days, totDuration / 3600, (totDuration % 3600) / 60));
            res.put("total", tot);
            
        	response.getWriter().append(res.toString());
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

	private Calendar getFirstDayOfYear(int year) {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		if (year!=-1) c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, Calendar.JANUARY);
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}
}
