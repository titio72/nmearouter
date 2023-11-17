/*
 * Copyright (c) 2022,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.data.track.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.data.Query;
import com.aboni.nmea.router.data.QueryById;
import com.aboni.nmea.router.data.track.TrackDumper;
import com.aboni.nmea.router.data.track.TrackManagementException;
import com.aboni.nmea.router.data.track.TrackPoint;
import com.aboni.nmea.router.data.track.TrackReader;
import com.aboni.nmea.router.data.track.impl.Track2GPX;
import com.aboni.nmea.router.data.track.impl.TrackPointBuilderImpl;
import net.sf.marineapi.nmea.util.Position;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class Track2GPXTest {


    private class MyTrackReader implements TrackReader {

        private TrackPoint getSample(long ts, double lat, double lon) {
            TrackPoint s = new TrackPointBuilderImpl()
                    .withPosition(new GeoPositionT(ts, new Position(lat, lon)))
                    .withPeriod(60).getPoint();
            return s;
        }

        @Override
        public void readTrack(Query q, TrackReaderListener target) throws TrackManagementException {
            target.onRead(1001, getSample(10001000, 43.10000000, 10.08000000));
            target.onRead(1002, getSample(10061000, 43.10000100, 10.08000100));
            target.onRead(1003, getSample(10121000, 43.10000200, 10.08000200));
            target.onRead(1004, getSample(10181000, 43.10000300, 10.08000300));
        }
    }

    @Test
    public void testTrackDefaultName() throws Exception {
        TrackDumper g = new Track2GPX(new MyTrackReader());
        StringWriter w = new StringWriter();
        g.dump(new QueryById(1), w);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document d = builder.parse(new ByteArrayInputStream(w.toString().getBytes()));

        assertEquals(Track2GPX.DEFAULT_TRACK_NAME, d.getElementsByTagName("name").item(0).getTextContent());
    }

}