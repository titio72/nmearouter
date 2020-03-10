package com.aboni.nmea.router.track.impl;

import com.aboni.nmea.router.track.*;
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
        public void readTrack(@NotNull TrackQuery query, @NotNull TrackReaderListener target) throws TrackManagementException {
            assertTrue(query instanceof TrackQueryByDate);
            assertEquals(from, ((TrackQueryByDate) query).getFrom());
            assertEquals(to, ((TrackQueryByDate) query).getTo());
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
        TrackAnalyticsByDate t = new TrackAnalyticsByDate(new MyTrackReader(from.toInstant(), to.toInstant()));
        JSONObject j = t.getAnalysis(from.toInstant(), to.toInstant());
        System.out.println(j.toString(2));
    }
}