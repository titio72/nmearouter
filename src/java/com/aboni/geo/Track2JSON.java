package com.aboni.geo;

import com.aboni.geo.PositionHistory.DoWithPoint;
import org.json.JSONObject;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class Track2JSON implements TrackDumper {

	public class PointWriter implements DoWithPoint {

		private static final boolean TRACK_THEM_ALL = true;

		private GeoPositionT previous;

		PointWriter() {
		}
		
		@Override
		public void doWithPoint(GeoPositionT p) {
			handlePoint(p);
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
			if (pr!=null) {
				Course c = new Course(pr, p);
				trackIt = (c.getDistance()>0.0025 /* NMg ~5m*/);
			}
			return trackIt;
		}
	}

	private PositionHistory track;
	private final JSONObject jsonTrack;
	private List<JSONObject> innerPath;
	private String trackName;
	public static final String DEFAULT_TRACK_NAME = "track";
	public static final String JSON_TRACK_TAG = "track";

	public Track2JSON() {
		jsonTrack = new JSONObject();
		trackName = DEFAULT_TRACK_NAME;
	}
	
  	public void setTrackName(String trackName) {
  		this.trackName = trackName;
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
		track.iterate(new PointWriter());
		((JSONObject)jsonTrack.get(JSON_TRACK_TAG)).put("path", getPath());
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
	public void setTrack(PositionHistory track) {
		this.track = track;
	}

}