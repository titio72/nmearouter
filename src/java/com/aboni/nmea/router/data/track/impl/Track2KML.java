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

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.data.track.TrackDumper;
import com.aboni.nmea.router.data.track.TrackManagementException;
import com.aboni.nmea.router.data.track.TrackPoint;
import com.aboni.nmea.router.data.track.TrackReader;
import com.aboni.nmea.router.utils.Query;
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
    public void dump(@NotNull Query query, @NotNull Writer w) throws TrackManagementException {
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
    public void setTrackName(@NotNull String trackName) {
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