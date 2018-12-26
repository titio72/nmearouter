package com.aboni.geo;

import com.aboni.geo.PositionHistory.DoWithPoint;
import org.json.JSONObject;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class Track2JSON implements TrackDumper {

	public class PointWriter implements DoWithPoint {

		PointWriter() {
		}
		
		@Override
		public void doWithPoint(GeoPositionT p) {
			handlePoint(p);
		}

	}

	private PositionHistory track;
	private final JSONObject jsonTrack;
	private List<JSONObject> innerPath;
    private GeoPositionT previous;
	private String trackName;
  	private static final boolean TRACK_THEM_ALL = true;
	public static final String DEFAULT_TRACK_NAME = "track";
	
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
		((JSONObject)jsonTrack.get("track")).put("path", getPath());

	}
	
	private boolean trackIt(GeoPositionT p, GeoPositionT pr) {
        boolean trackIt = true;
        if (pr!=null) {
            Course c = new Course(pr, p);
            trackIt = (c.getDistance()>0.0025 /* NMg ~5m*/); 
        }
        return trackIt;
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

	private void createPath() {
		jsonTrack.put("track", new JSONObject());
		((JSONObject)jsonTrack.get("track")).put("name", trackName);
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

/*
<?xml version="1.0" encoding="UTF-8" standalone="no" ?>

<gpx xmlns="http://www.topografix.com/GPX/1/1" xmlns:gpxx="http://www.garmin.com/xmlschemas/GpxExtensions/v3" xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1" creator="Oregon 400t" version="1.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd">
  <metadata>
    <link href="http://www.garmin.com">
      <text>Garmin International</text>
    </link>
    <time>2009-10-17T22:58:43Z</time>
  </metadata>
  <trk>
    <name>Example GPX Document</name>
    <trkseg>
      <trkpt lat="47.644548" lon="-122.326897">
        <ele>4.46</ele>
        <time>2009-10-17T18:37:26Z</time>
      </trkpt>
      <trkpt lat="47.644548" lon="-122.326897">
        <ele>4.94</ele>
        <time>2009-10-17T18:37:31Z</time>
      </trkpt>
      <trkpt lat="47.644548" lon="-122.326897">
        <ele>6.87</ele>
        <time>2009-10-17T18:37:34Z</time>
      </trkpt>
    </trkseg>
  </trk>
</gpx>*/


/*

      <LineString>
        <extrude>1</extrude>
        <tessellate>1</tessellate>
        <altitudeMode>absolute</altitudeMode>
        <coordinates> -112.2550785337791,36.07954952145647,2357
          -112.2549277039738,36.08117083492122,2357
          -112.2552505069063,36.08260761307279,2357
          -112.2564540158376,36.08395660588506,2357
          -112.2580238976449,36.08511401044813,2357
          -112.2595218489022,36.08584355239394,2357
          -112.2608216347552,36.08612634548589,2357
          -112.262073428656,36.08626019085147,2357
          -112.2633204928495,36.08621519860091,2357
          -112.2644963846444,36.08627897945274,2357
          -112.2656969554589,36.08649599090644,2357 
        </coordinates>
      </LineString>


*/