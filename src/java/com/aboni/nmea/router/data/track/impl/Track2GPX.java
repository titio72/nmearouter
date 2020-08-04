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

package com.aboni.nmea.router.data.track.impl;

import com.aboni.geo.Course;
import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.data.track.TrackDumper;
import com.aboni.nmea.router.data.track.TrackManagementException;
import com.aboni.nmea.router.data.track.TrackPoint;
import com.aboni.nmea.router.data.track.TrackReader;
import com.aboni.utils.Query;
import com.aboni.utils.ServerLog;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Track2GPX implements TrackDumper {

    private class PointWriter implements TrackReader.TrackReaderListener {

        private static final boolean TRACK_THEM_ALL = true;
        final Writer theWriter;
        private GeoPositionT previous;
        private GeoPositionT lastTrack;

        PointWriter(Writer w) {
            theWriter = w;
        }

        private boolean trackIt(GeoPositionT p, GeoPositionT pr) {
            boolean trackIt = true;
            if (pr != null) {
                Course c = new Course(pr, p);
                trackIt = (c.getDistance() > 0.0025 /* NMg ~5m*/);
            }
            return trackIt;
        }

        private void writePoint(GeoPositionT p) throws IOException {
            String s = "<trkpt lat=\"" + p.getLatitude() +
                    "\" lon=\"" + p.getLongitude() +
                    "\"><time>" + df.format(new Date(p.getTimestamp())) +
                    "</time></trkpt>\n";
            theWriter.write(s);
        }

        @Override
        public void onRead(TrackPoint sample) {
            try {
                GeoPositionT p = sample.getPosition();
                if (TRACK_THEM_ALL) {
                    writePoint(p);
                } else {
                    if (trackIt(p, previous)) {
                        if (lastTrack != null && (p.getTimestamp() - lastTrack.getTimestamp()) > 3600000L) {
                            theWriter.write("</trkseg><trkseg>");
                            writePoint(previous);
                        }
                        lastTrack = p;
                        writePoint(p);
                    }
                }
                previous = p;
            } catch (IOException e) {
                ServerLog.getLogger().error("Track2GPX Error writing GPX", e);
            }
        }
    }

    private final TrackReader track;
    private String trackName = DEFAULT_TRACK_NAME;
    public static final String DEFAULT_TRACK_NAME = "track";
    private final DateFormat df;


    @Inject
    public Track2GPX(@NotNull TrackReader reader) {
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        track = reader;
    }

    @Override
    public void dump(@NotNull Query query, @NotNull Writer w) throws IOException, TrackManagementException {
        writeHeader(w);
        writePoints(query, w);
        writeFooter(w);
    }

    private void writeFooter(Writer w) throws IOException {
        String footer = "</trkseg></trk></gpx>";
        w.write(footer);
    }

    private void writePoints(Query query, Writer w) throws TrackManagementException {
        track.readTrack(query, new PointWriter(w));
    }

    private void writeHeader(Writer w) throws IOException {
        String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";
        String name = "<name>" + trackName + "</name><trkseg>\n";
        w.write(header);
        w.write(name);
    }

    @Override
    public String getTrackName() {
        return trackName;
    }

    @Override
    public void setTrackName(@NotNull String trackName) {
        this.trackName = trackName;
    }

    @Override
    public String getMime() {
        return "application/gpx+xml";
    }

    @Override
    public String getExtension() {
        return "gpx";
    }
}