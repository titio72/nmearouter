package com.aboni.nmea.router.services;

import com.aboni.nmea.router.track.TrackDumper;
import com.aboni.nmea.router.track.TrackDumperFactory;
import com.aboni.nmea.router.track.TrackReader;
import com.aboni.nmea.router.track.impl.DBTrackReader;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Calendar;

public class TrackService  implements WebService {

	public TrackService() {
		// nothing to initialize
	}

	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
        try {
            final Calendar cFrom = config.getParamAsDate("dateFrom", 0);
            final Calendar cTo = config.getParamAsDate("dateTo", 1);
            String f = config.getParameter("format", "gpx");
            boolean download = "1".equals(config.getParameter("download", "0"));

            TrackReader loader = new DBTrackReader((DBHelper db) -> {

                PreparedStatement st = db.getConnection().prepareStatement("select * from track where TS>=? and TS<?");
                st.setTimestamp(1, new Timestamp(cFrom.getTimeInMillis()));
                st.setTimestamp(2, new Timestamp(cTo.getTimeInMillis()));
                return st;
            });

            TrackDumper dumper = TrackDumperFactory.getDumper(f);

            if (dumper != null) {
                String mime = dumper.getMime();
                String fileName = "track." + dumper.getExtension();
                response.setContentType(mime);
                if (download) response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                dumper.setTrack(loader);
                dumper.dump(response.getWriter());
                response.ok();

            } else {
                response.setContentType("text/html;charset=utf-8");
                response.error("Unknown format '" + f + "'");
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
