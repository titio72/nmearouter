package com.aboni.nmea.router.data.track.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.data.track.*;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.aboni.nmea.router.utils.db.DBEventWriter;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.nmea.router.utils.db.Event;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

import static org.junit.Assert.*;

public class TripManagerXImplTest {

    TripManagerXImpl tm;
    MockDBEventWriter trackWriter;
    MockDBEventWriter tripWriter;

    private class MockDBEventWriter implements DBEventWriter {

        Event e;

        @Override
        public void write(Event e, Connection c) throws SQLException {
            this.e = e;
        }

        @Override
        public void reset() {
        }
    }

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new NMEARouterModule());
        ThingsFactory.setInjector(injector);
        TrackTestTableManager.setUp();
        TrackTestTableManager.addTestData();
        trackWriter = new MockDBEventWriter();
        tripWriter = new MockDBEventWriter();
        tm = new TripManagerXImpl("trip_test", "track_test", trackWriter, tripWriter);
        tm.init();
    }

    @After
    public void tearDown() throws Exception {
        TrackTestTableManager.tearDown();
    }

    private void assertTrips(Trip test, Trip trip) {
        assertEquals(test.getTrip(), trip.getTrip());
        assertEquals(test.getDistance(), trip.getDistance(), 0.0001);
        assertEquals(test.getTripDescription(), trip.getTripDescription());
        assertEquals(test.getStartTS(), trip.getStartTS());
        assertEquals(test.getEndTS(), trip.getEndTS());
    }

    @Test
    public void testLoad() throws TripManagerException {
        boolean atLeastOne = false;
        for (Object[] t : TrackTestTableManager.testTrips) {
            Trip test = TrackTestTableManager.getTrip(t);
            Trip trip = tm.getTrip(test.getTrip());
            assertNotNull(trip);
            assertTrips(test, trip);
            atLeastOne = true;
        }
        assertTrue(atLeastOne);
    }

    @Test
    public void testGetTripById() throws TripManagerException {
        Trip t = tm.getTrip(136);
        Trip test = TrackTestTableManager.getTrip(136);
        assertTrips(test, t);
    }

    @Test
    public void testGetTripById_notFound() throws TripManagerException {
        Trip t = tm.getTrip(999);
        assertNull(t);
    }

    @Test
    public void testGetTripByTimeExactStart() throws TripManagerException {
        Trip test = TrackTestTableManager.getTrip(135);
        Trip t = tm.getTrip(test.getStartTS());
        assertTrips(test, t);
    }

    @Test
    public void testGetTripByTimeExactEnd() throws TripManagerException {
        Trip test = TrackTestTableManager.getTrip(135);
        Trip t = tm.getTrip(test.getEndTS());
        assertTrips(test, t);
    }

    @Test
    public void testGetTripByTimeMidOfaTrip() throws TripManagerException {
        Trip test = TrackTestTableManager.getTrip(135);
        Instant midOfTheTrip = Instant.ofEpochMilli((test.getEndTS().toEpochMilli() + test.getEndTS().toEpochMilli()) / 2);
        Trip t = tm.getTrip(midOfTheTrip);
        assertTrips(test, t);
    }

    @Test
    public void testGetTripByTimeNoTripHere() throws TripManagerException {
        Instant betweenTwoTrips = Instant.parse("2020-03-01T00:00:00Z");
        Trip t = tm.getTrip(betweenTwoTrips);
        assertNull(t);
    }

    @Test
    public void testGetTripByTimeBeforeAnyTrip() throws TripManagerException {
        Instant betweenTwoTrips = Instant.parse("2010-03-01T00:00:00Z");
        Trip t = tm.getTrip(betweenTwoTrips);
        assertNull(t);
    }

    @Test
    public void testGetTripByTimeAfterAnyTrip() throws TripManagerException {
        Instant betweenTwoTrips = Instant.parse("2020-03-30T00:00:00Z");
        Trip t = tm.getTrip(betweenTwoTrips);
        assertNull(t);
    }

    @Test
    public void testGetCurrentTrip() {
        Trip t = TrackTestTableManager.getTrip(136);
        Instant timestamp = t.getEndTS().plusSeconds(30 * 60 /*30 minutes since last trip end time*/);
        Trip current = tm.getCurrentTrip(timestamp);
        assertNotNull(t);
        assertEquals(t.getTrip(), current.getTrip());
    }

    @Test
    public void testGetCurrentTripNone() {
        Trip t = TrackTestTableManager.getTrip(136);
        Instant timestamp = t.getEndTS().plusSeconds(6 * 60 * 60 /*6 hours since last trip end time - too much time*/);
        Trip current = tm.getCurrentTrip(timestamp);
        assertNull(current);
    }

    @Test
    public void testChangeDescription() throws TripManagerException, ClassNotFoundException, SQLException, MalformedConfigurationException {
        tm.setTripDescription(136, "Capraia 1");
        assertEquals("Capraia 1", tm.getTrip(136).getTripDescription());
        try (DBHelper db = new DBHelper(true)) {
            try (ResultSet rs = db.getConnection().createStatement().executeQuery("select id, description from trip_test where id=136")) {
                rs.next();
                assertEquals("Capraia 1", rs.getString("description"));
            }
        }
    }

    @Test
    public void testChangeDistance() throws TripManagerException, ClassNotFoundException, SQLException, MalformedConfigurationException {
        tm.updateTripDistance(135, 91.2);
        assertEquals(91.2, tm.getTrip(135).getDistance(), 0.0001);
        try (DBHelper db = new DBHelper(true)) {
            try (ResultSet rs = db.getConnection().createStatement().executeQuery("select id, dist from trip_test where id=135")) {
                rs.next();
                assertEquals(91.2, rs.getDouble("dist"), 0.0001);
            }
        }
    }

    @Test
    public void testChangeDescriptionNonExisting() {
        try {
            tm.setTripDescription(137, "does not exists");
            fail();
        } catch (TripManagerException e) {
        }
    }


    @Test
    public void testDelete() throws TripManagerException, ClassNotFoundException, SQLException, MalformedConfigurationException {
        tm.deleteTrip(136);
        try (DBHelper db = new DBHelper(true)) {
            try (ResultSet rs = db.getConnection().createStatement().executeQuery("select id, description from trip_test where id=136")) {
                assertFalse(rs.next());
                assertNull(tm.getTrip(136));
            }
        }
    }

    @Test
    public void testDeleteNonExisting() {
        try {
            tm.deleteTrip(137);
            fail();
        } catch (TripManagerException e) {
        }
    }

    @Test
    public void testOnTrackPointNewTrip() throws TripManagerException, ClassNotFoundException, SQLException, MalformedConfigurationException {
        Instant t = Instant.ofEpochMilli(System.currentTimeMillis());
        TrackPoint p = new TrackPointBuilderImpl().
                withPosition(new GeoPositionT(t.toEpochMilli(), 43.6484500, 10.2660330)).
                withPeriod(30).withSpeed(5.79, 7.1).withDistance(0.04826200).getPoint();
        tm.onTrackPoint(new TrackEvent(p));
        Trip trip = tm.getCurrentTrip(t);
        assertNotNull(trip);
        assertEquals(t, trip.getStartTS());
        assertEquals(0.04826200, trip.getDistance(), 0.00001);
        try (DBHelper db = new DBHelper(true)) {
            try (ResultSet rs = db.getConnection().createStatement().executeQuery("select id, description from trip_test where id=" + trip.getTrip())) {
                assertTrue(rs.next());
            }
        }
    }

    @Test
    public void testOnTrackPointAddToTrip() throws TripManagerException, ClassNotFoundException, SQLException {
        Trip testTrip = TrackTestTableManager.getTrip(136);
        System.out.println(testTrip.getEndTS());
        Instant t = testTrip.getEndTS().plusSeconds(30);

        TrackPoint p = new TrackPointBuilderImpl().
                withPosition(new GeoPositionT(t.toEpochMilli(), 43.6484500, 10.2660330)).
                withPeriod(30).withSpeed(5.79, 7.1).withDistance(0.04826200).getPoint();
        tm.onTrackPoint(new TrackEvent(p));
        Trip trip = tm.getCurrentTrip(t);
        assertNotNull(trip);
        assertEquals(136, trip.getTrip());
        assertEquals(testTrip.getDistance() + 0.04826200, trip.getDistance(), 0.00001);
        assertEquals(t, trip.getEndTS());
        assertEquals(136, ((TripEvent) (tripWriter.e)).getTrip().getTrip());
    }

    @Test
    public void testList() throws TripManagerException {
        List<Trip> l = tm.getTrips(false);
        assertEquals(8, l.size());
    }

    @Test
    public void testListPerYear() throws TripManagerException {
        List<Trip> l = tm.getTrips(2019, false);
        assertEquals(2, l.size());
    }
}