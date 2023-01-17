package com.aboni.nmea.router.data.metrics.impl;

import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.data.DataManagementException;
import com.aboni.nmea.router.data.DataReader;
import com.aboni.nmea.router.data.ImmutableSample;
import com.aboni.nmea.router.data.Sample;
import com.aboni.nmea.router.data.metrics.Metrics;
import com.aboni.nmea.router.data.metrics.WindStats;
import com.aboni.nmea.router.data.Query;
import com.aboni.nmea.router.data.QueryByDate;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WindStatsReaderImplTest {

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new NMEARouterModule());
        ThingsFactory.setInjector(injector);
        loadData();
    }

    @After
    public void tearDown() throws Exception {
    }

    private static Sample ws(long time, double min, double speed, double max) {
        return ImmutableSample.newInstance(time, Metrics.WIND_SPEED.getId(), min, speed, max);
    }

    private static Sample wd(long time, double min, double speed, double max) {
        return ImmutableSample.newInstance(time, Metrics.WIND_DIRECTION.getId(), min, speed, max);
    }

    private final List<Sample> lWindTestData = new ArrayList<>();

    private void addWind(int tSeconds, double ws, double wd) {
        long t = tSeconds * 1000L;
        lWindTestData.add(ws(t, ws * 0.9, ws, ws * 1.1));
        lWindTestData.add(wd(t, wd - 5, wd, wd + 5));
    }

    private void loadData() {
        int t = (int)(System.currentTimeMillis() / 1000L);
        addWind(t+=30, 11, 90);
        addWind(t+=30, 13, 90);
        addWind(t+=30, 11, 90);
        addWind(t+=30, 13, 90);
        addWind(t+=30, 11, 90);
        addWind(t+=30, 13, 90);
        addWind(t+=30, 11, 90);
        addWind(t+=30, 13, 90);
        addWind(t+=30, 11, 90);
        addWind(t+=30, 13, 90);
        addWind(t+=30, 24, 0);
        addWind(t+=30, 24, 0);
        addWind(t+=30, 24, 0);
        addWind(t+=30, 24, 0);
        addWind(t+=30, 24, 0);
        addWind(t+=30, 24, 0);
        addWind(t+=30, 24, 0);
        addWind(t+=30, 24, 0);
        addWind(t+=30, 24, 0);
        addWind(t+=30, 24, 0);
    }

    private static class MockDataReader implements DataReader {
        private final List<Sample> samples;

        private MockDataReader(List<Sample> s) {
            samples = s;
        }

        @Override
        public void readData(Query query, String tag, DataReaderListener target) throws DataManagementException {
            QueryByDate q = (QueryByDate) query;
            for (Sample s: samples) {
                if (!(q.getFrom().isAfter(s.getInstant()) || q.getTo().isBefore(s.getInstant())) && s.getTag().equals(tag)) {
                    target.onRead(s);
                }
            }
        }

        @Override
        public void readData(Query query, DataReaderListener target) throws DataManagementException {
            QueryByDate q = (QueryByDate) query;
            for (Sample s: samples) {
                if (!(q.getFrom().isAfter(s.getInstant()) || q.getTo().isBefore(s.getInstant()))) {
                    target.onRead(s);
                }
            }
        }
    }

    @Test
    public void testLoad() throws DataManagementException {
        Instant d0 = lWindTestData.get(0).getInstant();
        Instant d1 = lWindTestData.get(lWindTestData.size()-1).getInstant();
        WindStats stats = new WindStatsReaderImpl(
                new MockDataReader(lWindTestData), 30).getWindStats(new QueryByDate(d0, d1), 72);
        for (int i = 0; i<72; i++) {
            switch (i) {
                case 18 /* 90 deg */:
                    assertEquals(1.0, stats.getWindDistance(i), 0.0001);
                    assertEquals(13.0, stats.getWindMaxSpeed(i), 0.0001);
                    assertEquals(300, stats.getWindTime(i));
                    break;
                case 0 /*  deg */:
                    assertEquals(2.0, stats.getWindDistance(i), 0.0001);
                    assertEquals(24.0, stats.getWindMaxSpeed(i), 0.0001);
                    assertEquals(300, stats.getWindTime(i));
                    break;
                default:
                    assertEquals(0.0, stats.getWindDistance(i), 0.0001);
                    assertEquals(0.0, stats.getWindMaxSpeed(i), 0.0001);
                    assertEquals(0, stats.getWindTime(i));
                    break;
            }
        }
    }
}