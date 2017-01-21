package com.aboni.nmea.router.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.aboni.utils.DBHelper;
import com.aboni.utils.ServerLog;

public class CruisingDaysService implements WebService {
	
	class Trip {
		int tripId;
		Set<Date> dates = new TreeSet<Date>();
		
		Trip(int id) {
			tripId = id; 
		}
		
		Date getMinDate() {
			if (dates.isEmpty()) return null;
			else return dates.iterator().next();
		}
	}
	
	private Map<Integer, Trip> trips = new TreeMap<Integer, Trip>();
	private Map<Integer, String> tripDescs = new HashMap<Integer, String>();
	
	private int counter = 0;

	
	public CruisingDaysService() {
		loadTrips();
	}
	
	private void loadTrips() {
		DBHelper db = null;
		try {
            db = new DBHelper(true);
            Statement stm = db.getConnection().createStatement();
            ResultSet rs = stm.executeQuery("select id, description from trip");
            while (rs.next()) {
            	tripDescs.put(rs.getInt(1), rs.getString(2));
            }
		} catch (Exception e) {
			ServerLog.getLogger().Error("Error loading trips", e);
		} finally {
			try {
				db.close();
			} catch (Exception e2) {}
		}
	}

	private void addToTrip(Date d, Integer i) {
		if (i==null) {
			i = new Integer(--counter);
			Trip t = new Trip(i);
			t.dates.add(d);
			trips.put(i, t);
		} else {
			Trip t = trips.getOrDefault(i, null);
			if (t==null) {
				t = new Trip(i);
				trips.put(i, t);
			}
			t.dates.add(d);
		}
	}

	private String getTripLabel(int i) {
		return tripDescs.getOrDefault(i, "Trip " + i);
	}
	
	private List<Trip> sortIt() {
		List<Trip> triplist = new ArrayList<Trip>(trips.values());
		triplist.sort(new Comparator<Trip>() {

			@Override
			public int compare(Trip o1, Trip o2) {
				return -o1.getMinDate().compareTo(o2.getMinDate());
			}
		});
		return triplist;
	}
	
	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
		DBHelper db = null;
		try {
			DateFormat df = new SimpleDateFormat("yyyyMMdd");
			
	        response.setContentType("application/json");

            db = new DBHelper(true);
            PreparedStatement stm = db.getConnection().prepareStatement("select tripid, Date(TS) from track group by tripid, Date(TS)");
            ResultSet rs = stm.executeQuery();

            while (rs.next()) {
            	Date d = rs.getDate(2);
            	Integer i = rs.getInt(1);
            	addToTrip(d, (i==0)?null:i);
            }            
            List<Trip> triplist = sortIt(); 
            
            
        	response.getWriter().append("{\"trips\":[");
        	boolean firsttrip = true;
        	for (Trip t: triplist) {
        		if (firsttrip) {
        			firsttrip = false;
        		} else {
                	response.getWriter().append(",");
        		}
            	response.getWriter().append("{\"trip\":\"" + t.tripId + "\", ");
            	response.getWriter().append("\"tripLabel\":\"" + (t.tripId>0?getTripLabel(t.tripId):"") + "\", ");
            	response.getWriter().append("\"firstDate\":\"" + "\", ");
            	response.getWriter().append("\"lastDate\":\"" + "\", ");
            	
            	
            	response.getWriter().append("\"dates\":[");
            	boolean first = true;
            	Date fD = null;
            	Date lD = null;
            	for (Date d: t.dates) {
            		if (first) {
            			first = false;
            			fD = d;
            			lD = d;
            		} else {
                    	response.getWriter().append(",");
                    	lD = d;
            		}
                	response.getWriter().append("{\"day\":");
                	response.getWriter().append("\"" + DateFormat.getDateInstance(DateFormat.SHORT).format(d) + "\"");
                	response.getWriter().append(",\"ref\":");
                	response.getWriter().append("\"" + df.format(d) + "\"");
                	response.getWriter().append("}");
            	}
            	response.getWriter().append("], ");
            	response.getWriter().append("\"firstDay\":");
            	response.getWriter().append("\"" + df.format(fD) + "\"");
            	response.getWriter().append(",\"lastDay\":");
            	response.getWriter().append("\"" + df.format(lD) + "\"");
            	response.getWriter().append("}");

        	}
        	response.getWriter().append("]}");
        	
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
