/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.services;

import com.aboni.nmea.router.data.track.TrackDumper;
import com.aboni.nmea.router.data.track.TrackDumperFactory;
import com.aboni.nmea.router.data.track.TrackManagementException;
import com.aboni.nmea.router.utils.Log;
import com.aboni.nmea.router.utils.Query;
import com.aboni.utils.LogStringBuilder;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.IOException;

public class TrackService implements WebService {

    private static final String TEXT_HTML_CHARSET_UTF_8 = "text/html;charset=utf-8";

    private final QueryFactory queryFactory;
    private final TrackDumperFactory trackerDumperFactory;
    private final Log log;

    @Inject
    public TrackService(@NotNull Log log, QueryFactory queryFactory, TrackDumperFactory trackerDumperFactory) {
        this.queryFactory = queryFactory;
        this.log = log;
        this.trackerDumperFactory = trackerDumperFactory;
    }

    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {
        Query q = null;
        try {
            q = queryFactory.getQuery(config);
            String f = config.getParameter("format", "gpx");
            boolean download = "1".equals(config.getParameter("download", "0"));
            TrackDumper dumper = trackerDumperFactory.getDumper(f);
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
            String sQuery = "" + q;
            log.error(() -> LogStringBuilder.start("TrackService").wO("response").wV("query", sQuery).toString(), e);
            response.setContentType(TEXT_HTML_CHARSET_UTF_8);
            sendError(response, e.getMessage());
        }
    }

    private void sendError(ServiceOutput response, String s) {
        try {
            response.error(s);
        } catch (Exception ee) {
            log.error(() -> LogStringBuilder.start("TrackService").wO("response").toString(), ee);
        }
    }
}
