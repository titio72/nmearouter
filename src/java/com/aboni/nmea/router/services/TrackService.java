package com.aboni.nmea.router.services;

import java.io.IOException;
import java.util.Calendar;

import com.aboni.geo.Track2GPX;
import com.aboni.geo.Track2JSON;
import com.aboni.geo.Track2KML;
import com.aboni.geo.TrackDumper;
import com.aboni.geo.TrackLoader;
import com.aboni.geo.TrackLoaderDB;
import com.aboni.utils.ServerLog;

public class TrackService  implements WebService {

	public TrackService() {
	}

	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
        try {
        	Calendar c = Calendar.getInstance();
        	c.setTimeInMillis( c.getTimeInMillis() - (c.getTimeInMillis()%(24*60*60*1000)) );
            Calendar cFrom = config.getParamAsCalendar(config, "dateFrom", c, "yyyyMMdd");
            
            Calendar c1 = Calendar.getInstance();
            c1.setTime(cFrom.getTime());
        	c.setTimeInMillis( c.getTimeInMillis() - (c.getTimeInMillis()%(24*60*60*1000)) );
        	Calendar cTo = config.getParamAsCalendar(config, "dateTo", c1, "yyyyMMdd");
        	cTo.add(Calendar.HOUR, 24);
        	
        	String f = config.getParameter("format");
        	if (f==null) f = "gpx";

        	String d = config.getParameter("download");
        	if (d==null) d = "0";
        	boolean download = "1".equals(d);
        	
			TrackLoader loader = new TrackLoaderDB();
			if (loader.load(cFrom, cTo)) {
				TrackDumper dumper = null;
				String mime = null;
				String fileName = null;

				switch (f) {
					case "gpx":
						mime = "application/gpx+xml";
						fileName = "track.gpx";
						dumper = new Track2GPX();
						break;
					case "kml":
						mime = "application/vnd.google-earth.kml+xml";
						fileName = "track.kml";
						dumper = new Track2KML();
						break;
					case "json":
						mime = "application/json";
						fileName = "track.json";
						dumper = new Track2JSON();
						break;
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
        } catch (IOException e) {
			ServerLog.getLogger().Error("Error downloading track", e);
            response.setContentType("text/html;charset=utf-8");
            try {
            	response.error(e.getMessage());
            } catch (Exception ee) {
				ServerLog.getLogger().Error("Error downloading track", ee);
			}
        }
		
	}
}
