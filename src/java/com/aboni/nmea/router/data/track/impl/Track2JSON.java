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
import com.aboni.nmea.router.data.Query;
import org.json.JSONObject;

import javax.inject.Inject;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class Track2JSON implements TrackDumper {

    public class PointWriter implements TrackReader.TrackReaderListener {

        private static final boolean TRACK_THEM_ALL = true;

        private TrackPoint previous;

        private PointWriter() {
        }

        @Override
        public void onRead(int id, TrackPoint sample) {
            handlePoint(sample);
        }

        private void handlePoint(TrackPoint p) {
            if (TRACK_THEM_ALL) {
                writePoint(p);
            } else {
                if (trackIt(p.getPosition(), previous.getPosition())) {
                    writePoint(p);
                }
            }
            previous = p;
        }

        private void writePoint(TrackPoint p) {
            JSONObject pt = new JSONObject();
            pt.put("lat", p.getPosition().getLatitude());
            pt.put("lng", p.getPosition().getLongitude());
            pt.put("time", p.getPosition().getTimestamp());
            pt.put("eng", p.getEngine().getValue());
            pt.put("anchor", p.isAnchor());
            pt.put("speed", p.getAverageSpeed());
            getPath().add(pt);
        }

        private boolean trackIt(GeoPositionT p, GeoPositionT pr) {
            boolean trackIt = true;
            if (pr != null) {
                Course c = new Course(pr, p);
                trackIt = (c.getDistance() > 0.0025 /* NMg ~5m*/);
            }
            return trackIt;
        }
    }

    private final TrackReader trackReader;
    private final JSONObject jsonTrack;
    private List<JSONObject> innerPath;
    private String trackName;
    public static final String DEFAULT_TRACK_NAME = "track";
    public static final String JSON_TRACK_TAG = "track";

    @Inject
    public Track2JSON(TrackReader trackReader) {
        if (trackReader==null) throw new IllegalArgumentException("Track reader is null");
        jsonTrack = new JSONObject();
        trackName = DEFAULT_TRACK_NAME;
        this.trackReader = trackReader;
    }

    @Override
    public void dump(Query query, Writer w) throws TrackManagementException {
        if (query==null) throw new TrackManagementException("The query provided is null");
        if (w==null) throw new TrackManagementException("Point writer is null");
        createPath();
        writePoints(query);
        writeIt(w);
    }

    private void writeIt(Writer w) {
        jsonTrack.write(w);
    }

    private void writePoints(Query query) throws TrackManagementException {
        PointWriter pw = new PointWriter();
        trackReader.readTrack(query, pw);
        ((JSONObject) jsonTrack.get(JSON_TRACK_TAG)).put("path", getPath());
    }

    private void createPath() {
        jsonTrack.put(JSON_TRACK_TAG, new JSONObject());
        ((JSONObject) jsonTrack.get(JSON_TRACK_TAG)).put("name", trackName);
        innerPath = new ArrayList<>();
    }

    private List<JSONObject> getPath() {
        return innerPath;
    }

    @Override
    public String getTrackName() {
        return trackName;
    }

    @Override
    public String getMime() {
        return "application/json";
    }

    @Override
    public String getExtension() {
        return "json";
    }

    @Override
    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }
}