package com.aboni.nmea.router.track.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.track.TrackDumper;
import com.aboni.nmea.router.track.TrackManagementException;
import com.aboni.nmea.router.track.TrackPoint;
import com.aboni.nmea.router.track.TrackReader;
import com.aboni.utils.Query;
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

    private final TrackReader track;
    private final Kml kml;

    @Inject
    public Track2KML(@NotNull TrackReader reader) {
        kml = new Kml();
        track = reader;
    }

    @Override
    public void dump(Query query, Writer w) throws TrackManagementException {
        LineString s = createString();
        writePoints(query, s);
        kml.marshal(w);
    }

    private void writePoints(Query query, LineString w) throws TrackManagementException {
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