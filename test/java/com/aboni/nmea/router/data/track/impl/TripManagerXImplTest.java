package com.aboni.nmea.router.data.track.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.NMEAMessagesModule;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.data.track.TrackEvent;
import com.aboni.nmea.router.data.track.TrackPoint;
import com.aboni.nmea.router.data.track.Trip;
import com.aboni.nmea.router.data.track.TripManagerException;
import com.aboni.log.ConsoleLog;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.sensors.EngineStatus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class TripManagerXImplTest {

    private TripManagerXImpl tm;

    private int last_trip = -1;
    private int first_trip = -1;
    private int a_trip = -1;

    private int no_trip = -1;

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new NMEARouterModule(), new NMEAMessagesModule());
        ThingsFactory.setInjector(injector);
        TrackTestTableManager.setUp();
        TrackTestTableManager.addTestData();
        first_trip = (int) TrackTestTableManager.testTrips[0][0];
        a_trip = (int) TrackTestTableManager.testTrips[TrackTestTableManager.testTrips.length-2][0];
        last_trip = (int) TrackTestTableManager.testTrips[TrackTestTableManager.testTrips.length-1][0];
        no_trip = last_trip + 1; // non existent trip
        tm = new TripManagerXImpl(ConsoleLog.getLogger(), "trip_test", "track_test");
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
        Trip t = tm.getTrip(last_trip);
        Trip test = TrackTestTableManager.getTrip(last_trip);
        assertTrips(test, t);
    }

    @Test
    public void testGetTripById_notFound() throws TripManagerException {
        Trip t = tm.getTrip(no_trip);
        assertNull(t);
    }

    @Test
    public void testGetTripByTimeExactStart() throws TripManagerException {
        Trip test = TrackTestTableManager.getTrip(a_trip);
        Trip t = tm.getTrip(test.getStartTS());
        assertTrips(test, t);
    }

    @Test
    public void testGetTripByTimeExactEnd() throws TripManagerException {
        Trip test = TrackTestTableManager.getTrip(a_trip);
        Trip t = tm.getTrip(test.getEndTS());
        assertTrips(test, t);
    }

    @Test
    public void testGetTripByTimeMidOfaTrip() throws TripManagerException {
        Trip test = TrackTestTableManager.getTrip(a_trip);
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
        Trip t = TrackTestTableManager.getTrip(last_trip);
        Instant timestamp = t.getEndTS().plusSeconds(30 * 60 /*30 minutes since last trip end time*/);
        Trip current = tm.getCurrentTrip(timestamp);
        assertNotNull(t);
        assertEquals(t.getTrip(), current.getTrip());
    }

    @Test
    public void testGetCurrentTripNone() {
        Trip t = TrackTestTableManager.getTrip(last_trip);
        Instant timestamp = t.getEndTS().plusSeconds(6 * 60 * 60 /*6 hours since last trip end time - too much time*/);
        Trip current = tm.getCurrentTrip(timestamp);
        assertNull(current);
    }

    @Test
    public void testChangeDescription() throws TripManagerException, ClassNotFoundException, SQLException, MalformedConfigurationException {
        tm.setTripDescription(last_trip, "Capraia 1");
        assertEquals("Capraia 1", tm.getTrip(last_trip).getTripDescription());
        try (DBHelper db = new DBHelper(ConsoleLog.getLogger(), true)) {
            try (ResultSet rs = db.getConnection().createStatement().executeQuery("select id, description from trip_test where id=136")) {
                rs.next();
                assertEquals("Capraia 1", rs.getString("description"));
            }
        }
    }

    @Test
    public void testChangeDistance() throws TripManagerException, ClassNotFoundException, SQLException, MalformedConfigurationException {
        tm.updateTripDistance(a_trip, 91.2);
        assertEquals(91.2, tm.getTrip(a_trip).getDistance(), 0.0001);
        try (DBHelper db = new DBHelper(ConsoleLog.getLogger(), true)) {
            try (ResultSet rs = db.getConnection().createStatement().executeQuery("select id, dist from trip_test where id=" + a_trip)) {
                rs.next();
                assertEquals(91.2, rs.getDouble("dist"), 0.0001);
            }
        }
    }

    @Test
    public void testChangeDescriptionNonExisting() {
        try {
            tm.setTripDescription(no_trip, "does not exists");
            fail();
        } catch (TripManagerException e) {
        }
    }


    @Test
    public void testDelete() throws TripManagerException, ClassNotFoundException, SQLException, MalformedConfigurationException {
        tm.deleteTrip(last_trip);
        try (DBHelper db = new DBHelper(ConsoleLog.getLogger(), true)) {
            try (ResultSet rs = db.getConnection().createStatement().executeQuery("select id, description from trip_test where id=136")) {
                assertFalse(rs.next());
                assertNull(tm.getTrip(last_trip));
            }
        }
    }

    @Test
    public void testDeleteNonExisting() {
        try {
            tm.deleteTrip(no_trip);
            fail();
        } catch (TripManagerException e) {
        }
    }

    @Test
    public void testStartNewTrip() throws Exception {
        Instant t = Instant.ofEpochMilli(System.currentTimeMillis());
        TrackPoint p = new TrackPointBuilderImpl().
                withPosition(new GeoPositionT(t.toEpochMilli(), 43.6484500, 10.2660330)).
                withPeriod(30).withSpeed(5.79, 7.1).withDistance(0.04826200).withEngine(EngineStatus.ON).getPoint();
        tm.onTrackPoint(new TrackEvent(p));
        Trip trip = tm.getCurrentTrip(t);
        assertNotNull(trip);
        assertEquals(t, trip.getStartTS());
        assertEquals(t, trip.getEndTS());
        assertEquals(0.04826200, trip.getDistance(), 0.00001);
        assertEquals(0.00000000, trip.getDistanceSail(), 0.00001);
        assertEquals(0.04826200, trip.getDistanceMotor(), 0.00001);
        checkDB(trip);
    }

    private void checkDB(Trip t) throws Exception {
        try (DBHelper db = new DBHelper(ConsoleLog.getLogger(), true)) {
            try (ResultSet rs = db.getConnection().createStatement().executeQuery("select * from trip_test where id=" + t.getTrip())) {
                assertTrue(rs.next());
                assertEquals(roundSecond(t.getStartTS()), rs.getTimestamp("fromTS", Calendar.getInstance(TimeZone.getTimeZone("UTC"))).toInstant());
                assertEquals(roundSecond(t.getEndTS()), rs.getTimestamp("toTS", Calendar.getInstance(TimeZone.getTimeZone("UTC"))).toInstant());
                assertEquals(t.getDistance(), rs.getDouble("dist"), 0.00001);
                assertEquals(t.getDistanceSail(), rs.getDouble("distSail"), 0.00001);
                assertEquals(t.getDistanceMotor(), rs.getDouble("distMotor"), 0.00001);
            }
        }
    }

    private static Instant roundSecond(Instant t) {
        // have to do it because apparently MySQL truncate the millisecond part...
        return Instant.ofEpochMilli((t.toEpochMilli()/1000)*1000);
    }

    @Test
    public void testOnTrackPointAddToTrip() throws Exception {
        Trip testTrip = TrackTestTableManager.getTrip(last_trip);
        Instant t = testTrip.getEndTS().plusSeconds(30); // simulate a point 30 seconds from the end of the lsat trip
        TrackPoint p = new TrackPointBuilderImpl().
                withPosition(new GeoPositionT(t.toEpochMilli(), 43.6484500, 10.2660330)).
                withPeriod(30).withSpeed(5.79, 7.1).withDistance(0.04826200).withEngine(EngineStatus.ON).getPoint();
        tm.onTrackPoint(new TrackEvent(p)); // make 0.04826200 with engine on
        Trip trip = tm.getCurrentTrip(t);
        assertNotNull(trip);
        assertEquals(last_trip, trip.getTrip());
        assertEquals(testTrip.getDistance() + 0.04826200, trip.getDistance(), 0.00001);
        assertEquals(testTrip.getDistanceMotor() + 0.04826200, trip.getDistanceMotor(), 0.00001);
        assertEquals(testTrip.getDistanceSail(), trip.getDistanceSail(), 0.00001);
        assertEquals(trip.getEndTS(), t);
        checkDB(trip);
    }

    @Test
    public void testList() throws TripManagerException {
        List<Trip> l = tm.getTrips(false);
        assertEquals(TrackTestTableManager.testTrips.length, l.size());
    }

    @Test
    public void testListPerYear() throws TripManagerException {
        List<Trip> l = tm.getTrips(2020, false);
        assertEquals(TrackTestTableManager.testTrips.length, l.size());

        l = tm.getTrips(2019, false);
        assertEquals(0, l.size());
    }
}