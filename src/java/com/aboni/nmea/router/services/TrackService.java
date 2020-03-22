package com.aboni.nmea.router.services;

import com.aboni.nmea.router.track.TrackDumper;
import com.aboni.nmea.router.track.TrackDumperFactory;
import com.aboni.nmea.router.track.TrackManagementException;
import com.aboni.utils.Query;
import com.aboni.utils.ServerLog;

import javax.inject.Inject;
import java.io.IOException;

public class TrackService implements WebService {

    private static final String TEXT_HTML_CHARSET_UTF_8 = "text/html;charset=utf-8";
    private static final String ERROR_DOWNLOADING_TRACK = "Error downloading track";

    @Inject
    public TrackService() {
        // nothing to initialize
    }

    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {
        try {
            Query q = QueryFactory.getQuery(config);
            String f = config.getParameter("format", "gpx");
            boolean download = "1".equals(config.getParameter("download", "0"));
            TrackDumper dumper = TrackDumperFactory.getDumper(f);
            if (dumper != null) {
                String mime = dumper.getMime();
                String fileName = "track." + dumper.getExtension();
                response.setContentType(mime);
                if (download)
                    response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                dumper.dump(q, response.getWriter());
                response.ok();
            } else {
                response.setContentType(TEXT_HTML_CHARSET_UTF_8);
                response.error("Unknown format '" + f + "'");
            }
        } catch (IOException | TrackManagementException e) {
            ServerLog.getLogger().error(ERROR_DOWNLOADING_TRACK, e);
            response.setContentType(TEXT_HTML_CHARSET_UTF_8);
            sendError(response, e.getMessage());
        }
    }

    private void sendError(ServiceOutput response, String s) {
        try {
            response.error(s);
        } catch (Exception ee) {
            ServerLog.getLogger().error(ERROR_DOWNLOADING_TRACK, ee);
        }
    }
}
