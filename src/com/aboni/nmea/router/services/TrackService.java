package com.aboni.nmea.router.services;

import java.util.Calendar;

import com.aboni.geo.Track2GPX;
import com.aboni.geo.Track2JSON;
import com.aboni.geo.Track2KML;
import com.aboni.geo.TrackLoader;
import com.aboni.geo.TrackLoaderDB;

public class TrackService  implements WebService {

	public TrackService() {
	}

	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
        try {
        	Calendar c = Calendar.getInstance();
        	c.setTimeInMillis( c.getTimeInMillis() - (c.getTimeInMillis()%(24*60*60*1000)) );
            Calendar cFrom = config.getParamAsCalendar(config, "from", c, "yyyyMMdd");
            
            Calendar c1 = Calendar.getInstance();
            c1.setTime(cFrom.getTime());
        	c.setTimeInMillis( c.getTimeInMillis() - (c.getTimeInMillis()%(24*60*60*1000)) );
        	Calendar cTo = config.getParamAsCalendar(config, "to", c1, "yyyyMMdd");
        	cTo.add(Calendar.HOUR, 24);
        	
        	String f = config.getParameter("format");
        	if (f==null) f = "gpx";
        	
        	if (cFrom!=null && cTo!=null) {
	        	TrackLoader loader = new TrackLoaderDB();
	            if (loader.load(cFrom, cTo)) {
		            if (f.equals("gpx")) {
			            response.setContentType("application/octet-stream");
			            response.setHeader("Content-Disposition", "attachment; filename=\"track.gpx\"");
		                new Track2GPX(loader.getTrack()).dump(response.getWriter());
		                response.ok();
		            } else if (f.equals("kml")) {
			            response.setContentType("application/octet-stream");
			            response.setHeader("Content-Disposition", "attachment; filename=\"track.gpx\"");
	                    new Track2KML(loader.getTrack()).dump(response.getWriter());
	                    response.ok();
		            } else if (f.equals("json")) {
		            	response.setContentType("application/json");
		            	new Track2JSON(loader.getTrack()).dump(response.getWriter());
	                    response.ok();
		            } else {
		                response.setContentType("text/html;charset=utf-8");
		                response.error("Unknown format '" + f + "'");
		            }
	            }
        	} else {
                response.setContentType("text/html;charset=utf-8");
                response.error("Cannot read parameters");
        	}
        } catch (Exception e) {
            response.setContentType("text/html;charset=utf-8");
            response.error(e.getMessage());
        }
		
	}
}
