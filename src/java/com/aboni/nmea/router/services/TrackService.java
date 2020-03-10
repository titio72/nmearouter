package com.aboni.nmea.router.services;

import com.aboni.nmea.router.track.*;
import com.aboni.utils.ServerLog;

import java.io.IOException;
import java.time.Instant;

public class TrackService implements WebService {

    private static final String TEXT_HTML_CHARSET_UTF_8 = "text/html;charset=utf-8";
    private static final String ERROR_DOWNLOADING_TRACK = "Error downloading track";

    public TrackService() {
        // nothing to initialize
    }

    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {
        try {
            TrackQuery q = getTrackQuery(config);
            if (q != null) {
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
            } else {
                response.setContentType(TEXT_HTML_CHARSET_UTF_8);
                sendError(response, "No valid query defined");
            }
        } catch (IOException | TrackManagementException e) {
            ServerLog.getLogger().error(ERROR_DOWNLOADING_TRACK, e);
            response.setContentType(TEXT_HTML_CHARSET_UTF_8);
            sendError(response, e.getMessage());
        }
    }

    private TrackQuery getTrackQuery(ServiceConfig config) {
        final int trip = config.getInteger("trip", -1);
        TrackQuery q = null;
        if (trip != -1) {
            q = new TrackQueryById(trip);
        } else {
            final Instant cFrom = config.getParamAsInstant("dateFrom", null, 0);
            final Instant cTo = config.getParamAsInstant("dateTo", null, 1);
            if (cFrom != null && cTo != null) q = new TrackQueryByDate(cFrom, cTo);
        }
        return q;
    }

    private void sendError(ServiceOutput response, String s) {
        try {
            response.error(s);
        } catch (Exception ee) {
            ServerLog.getLogger().error(ERROR_DOWNLOADING_TRACK, ee);
        }
    }
}
