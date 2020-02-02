package com.aboni.utils.db;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.OffsetDateTime;

public class TrackAnalyticsTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testRun() throws Exception {
        TrackAnalytics t = new TrackAnalytics();
        OffsetDateTime from = OffsetDateTime.parse("2020-01-12T00:00:00+01:00");
        OffsetDateTime to = OffsetDateTime.parse("2020-01-13T00:00:00+01:00");
        TrackAnalytics.Stats res = t.run(from.toInstant(), to.toInstant());
        System.out.println(res.toJson().toString(2));
    }
}