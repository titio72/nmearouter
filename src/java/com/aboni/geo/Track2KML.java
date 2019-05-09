package com.aboni.geo;

import com.aboni.geo.PositionHistory.DoWithPoint;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;

import java.io.Writer;

public class Track2KML implements TrackDumper {

	public class PointWriter implements DoWithPoint {

		final LineString theWriter;
		
		PointWriter(LineString w) {
			theWriter = w;
		}
		
		@Override
		public void doWithPoint(GeoPositionT p) {
			if (TRACK_THEM_ALL ) {
				theWriter.addToCoordinates(p.getLatitude(), p.getLongitude());
			}
		}

	}

	private PositionHistory track;
	private final Kml kml;
	
	public Track2KML() {
	    kml = new Kml();
	}

	@Override
	public void setTrack(PositionHistory track) {
		this.track = track;
	}

	public void dump(Writer w) {
		if (track!=null) {
		    LineString s = createString();
			writePoints(s);
			kml.marshal(w);
		}
	}

	private void writePoints(LineString w) {
		track.iterate(new PointWriter(w));
	}
	
	private static final boolean TRACK_THEM_ALL = true;

	private LineString createString() {
		return kml.createAndSetPlacemark().withName("London, UK").withOpen(Boolean.TRUE).createAndSetLineString();
	}

	@Override
	public String getTrackName() {
		return "";
	}

	@Override
	public void setTrackName(String trackName) {
		// unsupported
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