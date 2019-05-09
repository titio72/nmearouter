package com.aboni.nmea.router.services;

import com.aboni.geo.*;
import com.aboni.utils.ServerLog;

import java.io.IOException;
import java.util.Calendar;

public class TrackService  implements WebService {

	public TrackService() {
		// nothing to initialize
	}

	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
        try {
        	Calendar cFrom = config.getParamAsDate("dateFrom", 0);
            Calendar cTo = config.getParamAsDate("dateTo", 1);
        	String f = config.getParameter("format", "gpx");
        	boolean download = "1".equals(config.getParameter("download", "0"));
        	
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
					default:
						// do nothing to do
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
			ServerLog.getLogger().error("Error downloading track", e);
            response.setContentType("text/html;charset=utf-8");
            try {
            	response.error(e.getMessage());
            } catch (Exception ee) {
				ServerLog.getLogger().error("Error downloading track", ee);
			}
        }
		
	}
}
