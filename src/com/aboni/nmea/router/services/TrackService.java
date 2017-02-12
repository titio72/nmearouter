package com.aboni.nmea.router.services;

import java.util.Calendar;

import com.aboni.geo.Track2GPX;
import com.aboni.geo.Track2JSON;
import com.aboni.geo.Track2KML;
import com.aboni.geo.TrackDumper;
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

        	String d = config.getParameter("download");
        	if (d==null) d = "0";
        	boolean download = "1".equals(d);
        	
        	if (cFrom!=null && cTo!=null) {
	        	TrackLoader loader = new TrackLoaderDB();
	            if (loader.load(cFrom, cTo)) {
	            	TrackDumper dumper = null;
	            	String mime = null;
	            	String fileName = null;
	            	
		            if (f.equals("gpx")) {
			            mime = "application/gpx+xml";
			            fileName = "track.gpx";
		                dumper = new Track2GPX();
		            } else if (f.equals("kml")) {
			            mime = "application/vnd.google-earth.kml+xml";
			            fileName = "track.kml";
	                    dumper = new Track2KML();
		            } else if (f.equals("json")) {
		            	mime = "application/json";
		            	fileName = "track.json";
		            	dumper = new Track2JSON();
		            }

		            if (dumper!=null) {
			            response.setContentType(mime);
			            if (download) response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		            	dumper.setTrack(loader.getTrack());
		            	dumper.dump(response.getWriter());
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
