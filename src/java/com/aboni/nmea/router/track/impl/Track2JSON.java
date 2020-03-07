package com.aboni.nmea.router.track.impl;

import com.aboni.geo.Course;
import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.track.TrackDumper;
import com.aboni.nmea.router.track.TrackManagementException;
import com.aboni.nmea.router.track.TrackPoint;
import com.aboni.nmea.router.track.TrackReader;
import com.aboni.utils.ServerLog;
import org.json.JSONObject;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class Track2JSON implements TrackDumper {

    public class PointWriter implements TrackReader.TrackReaderListener {

        private static final boolean TRACK_THEM_ALL = true;

        private GeoPositionT previous;

        PointWriter() {
        }

        @Override
        public void onRead(TrackPoint sample) {
            handlePoint(sample.getPosition());
        }

		private void handlePoint(GeoPositionT p) {
			if (TRACK_THEM_ALL ) {
				writePoint(p);
			} else {
				if (trackIt(p, previous)) {
					writePoint(p);
				}
			}
			previous = p;
		}

		private void writePoint(GeoPositionT p) {
			JSONObject pt = new JSONObject();
			pt.put("lat", p.getLatitude());
			pt.put("lng", p.getLongitude());
			pt.put("time", p.getTimestamp());
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

    private TrackReader track;
    private final JSONObject jsonTrack;
    private List<JSONObject> innerPath;
    private String trackName;
    public static final String DEFAULT_TRACK_NAME = "track";
    public static final String JSON_TRACK_TAG = "track";

    public Track2JSON() {
        jsonTrack = new JSONObject();
        trackName = DEFAULT_TRACK_NAME;
    }

	public void dump(Writer w) {
		if (track!=null) {
			createPath();
			writePoints();
			writeIt(w);
		}
	}

	private void writeIt(Writer w) {
		jsonTrack.write(w);
	}

	private void writePoints() {
        PointWriter pw = new PointWriter();
        if (track != null) {
            try {
                track.readTrack(pw);
            } catch (TrackManagementException e) {
                ServerLog.getLogger().error("Track2JSON Error reading track", e);
            }
        }
        ((JSONObject) jsonTrack.get(JSON_TRACK_TAG)).put("path", getPath());
    }

	private void createPath() {
		jsonTrack.put(JSON_TRACK_TAG, new JSONObject());
		((JSONObject)jsonTrack.get(JSON_TRACK_TAG)).put("name", trackName);
		innerPath = new ArrayList<>();
	}
	
	private List<JSONObject> getPath() {
		return innerPath;
	}

    @Override
    public void setTrack(TrackReader track) {
        this.track = track;
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