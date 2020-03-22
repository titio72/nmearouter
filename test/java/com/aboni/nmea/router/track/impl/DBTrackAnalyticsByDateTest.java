package com.aboni.nmea.router.track.impl;

import com.aboni.nmea.router.track.JSONTrackAnalytics;
import com.aboni.nmea.router.track.TrackManagementException;
import com.aboni.nmea.router.track.TrackReader;
import com.aboni.utils.Query;
import com.aboni.utils.QueryByDate;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.OffsetDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DBTrackAnalyticsByDateTest {

    private class MyTrackReader implements TrackReader {

        Instant from;
        Instant to;

        MyTrackReader(Instant from, Instant to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public void readTrack(@NotNull Query query, @NotNull TrackReaderListener target) throws TrackManagementException {
            assertTrue(query instanceof QueryByDate);
            assertEquals(from, ((QueryByDate) query).getFrom());
            assertEquals(to, ((QueryByDate) query).getTo());
        }
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRun() throws Exception {
        // TODO
        OffsetDateTime from = OffsetDateTime.parse("2020-01-12T00:00:00+01:00");
        OffsetDateTime to = OffsetDateTime.parse("2020-01-13T00:00:00+01:00");
        JSONTrackAnalytics t = new JSONTrackAnalytics(new MyTrackReader(from.toInstant(), to.toInstant()));
        JSONObject j = t.getAnalysis(new QueryByDate(from.toInstant(), to.toInstant()));
        System.out.println(j.toString(2));
    }
}