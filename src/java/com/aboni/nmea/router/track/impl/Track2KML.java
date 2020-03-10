package com.aboni.nmea.router.track.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.track.*;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.Writer;

public class Track2KML implements TrackDumper {

    public static class PointWriter implements TrackReader.TrackReaderListener {

        final LineString theWriter;

        PointWriter(LineString w) {
            theWriter = w;
        }

        @Override
        public void onRead(TrackPoint sample) {
            if (TRACK_THEM_ALL) {
                GeoPositionT p = sample.getPosition();
                theWriter.addToCoordinates(p.getLatitude(), p.getLongitude());
            }
        }
    }

    private TrackReader track;
    private final Kml kml;

    @Inject
    public Track2KML(@NotNull TrackReader reader) {
        kml = new Kml();
        track = reader;
    }

    @Override
    public void dump(TrackQuery query, Writer w) throws TrackManagementException {
        LineString s = createString();
        writePoints(query, s);
        kml.marshal(w);
    }

    private void writePoints(TrackQuery query, LineString w) throws TrackManagementException {
        track.readTrack(query, new PointWriter(w));
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

    @Override
    public String getMime() {
        return "application/vnd.google-earth.kml+xml";
    }

    @Override
    public String getExtension() {
        return "kml";
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