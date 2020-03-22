package com.aboni.geo;

import com.aboni.nmea.router.track.TrackDumper;
import com.aboni.nmea.router.track.TrackManagementException;
import com.aboni.nmea.router.track.TrackPoint;
import com.aboni.nmea.router.track.TrackReader;
import com.aboni.nmea.router.track.impl.Track2GPX;
import com.aboni.nmea.router.track.impl.TrackPointBuilderImpl;
import com.aboni.utils.Query;
import com.aboni.utils.QueryById;
import net.sf.marineapi.nmea.util.Position;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.validation.constraints.NotNull;
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
        public void readTrack(@NotNull Query q, @NotNull TrackReaderListener target) throws TrackManagementException {
            target.onRead(getSample(10001000, 43.10000000, 10.08000000));
            target.onRead(getSample(10061000, 43.10000100, 10.08000100));
            target.onRead(getSample(10121000, 43.10000200, 10.08000200));
            target.onRead(getSample(10181000, 43.10000300, 10.08000300));
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