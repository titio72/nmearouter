package com.aboni.geo;

import java.io.IOException;
import java.io.Writer;

import com.aboni.geo.PositionHistory.DoWithPoint;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;

public class Track2KML implements TrackDumper {

	public class PointWriter implements DoWithPoint {

		LineString theWriter;
		
		PointWriter(LineString w) {
			theWriter = w;
		}
		
		@Override
		public void doWithPoint(GeoPositionT p) {
			try {
				handlePoint(p, theWriter);
			} catch (IOException e) {
				// ??????
			}
		}

	}

	private PositionHistory track;
	private Kml kml;
	
	public Track2KML() {
	    kml = new Kml();
	}
	
	public PositionHistory getTrack() {
		return track;
	}

	@Override
	public void setTrack(PositionHistory track) {
		this.track = track;
	}

	public void dump(Writer w) throws IOException {
		if (track!=null) {
		    LineString s = createString();
			writePoints(s);
			kml.marshal(w);
		}
	}

	private void writePoints(LineString w) {
		track.iterate(new PointWriter(w));
		
	}
	
	private static boolean TRACK_THEM_ALL = true;
	
	private void handlePoint(GeoPositionT p, LineString pk) throws IOException {
		if (TRACK_THEM_ALL ) {
    	    writePoint(p, pk);
		}
	}

    private void writePoint(GeoPositionT p, LineString pk) throws IOException {
        pk.addToCoordinates(p.getLatitude(), p.getLongitude());
    }

	private LineString createString() throws IOException {
	    LineString pmk = kml.createAndSetPlacemark().withName("London, UK").withOpen(Boolean.TRUE).createAndSetLineString();
	    return pmk;
	}

	@Override
	public void setTrackName(String trackName) {
		// not supported
	}	
	
}

/*
<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2"
 xmlns:gx="http://www.google.com/kml/ext/2.2">   <!-- required when using gx-prefixed elements -->

<Placemark>
  <name>gx:altitudeMode Example</name>
  <LookAt>
    <longitude>146.806</longitude>
    <latitude>12.219</latitude>
    <heading>-60</heading>
    <tilt>70</tilt>
    <range>6300</range>
    <gx:altitudeMode>relativeToSeaFloor</gx:altitudeMode>
  </LookAt>
  <LineString>
    <extrude>1</extrude>
    <gx:altitudeMode>relativeToSeaFloor</gx:altitudeMode>
    <coordinates>
      146.825,12.233,400
      146.820,12.222,400
      146.812,12.212,400
      146.796,12.209,400
      146.788,12.205,400
    </coordinates>
  </LineString>
</Placemark>

</kml>

*/