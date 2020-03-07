package com.aboni.nmea.router.track.impl;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.OffsetDateTime;

public class DBTrackAnalyticsByDateTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRun() throws Exception {
        // TODO
        DBTrackAnalyticsByDate t = new DBTrackAnalyticsByDate();
        OffsetDateTime from = OffsetDateTime.parse("2020-01-12T00:00:00+01:00");
        OffsetDateTime to = OffsetDateTime.parse("2020-01-13T00:00:00+01:00");
        JSONObject j = t.getAnalysis(from.toInstant(), to.toInstant());
        System.out.println(j.toString(2));
    }
}