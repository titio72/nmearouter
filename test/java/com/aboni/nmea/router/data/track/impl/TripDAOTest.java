package com.aboni.nmea.router.data.track.impl;

import com.aboni.nmea.router.data.track.Trip;
import com.aboni.nmea.router.data.track.TripEvent;
import com.aboni.log.ConsoleLog;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.sensors.EngineStatus;
import com.aboni.utils.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.Assert.*;

public class TripDAOTest {

    private TripDAO tripDAO;

    @Before
    public void setUp() throws Exception {
        TrackTestTableManager.setUp();
        TrackTestTableManager.addTestData();
        tripDAO = new TripDAO("trip_test");
    }

    @After
    public void tearDown() throws Exception {
        TrackTestTableManager.tearDown();
    }

    @Test
    public void testUpdate() throws Exception {
        TripImpl trip = TrackTestTableManager.getTrip(136);
        trip.setTS(trip.getEndTS().plusSeconds(30));
        trip.addDistance(0.0543, EngineStatus.OFF);
        try (DBHelper db = new DBHelper(ConsoleLog.getLogger(), true)) {
            tripDAO.updateTrip(new TripEvent(trip), db.getConnection());
            check(db, trip);
        }
    }

    @Test
    public void testLoadArchive() throws Exception {
        try (DBHelper db = new DBHelper(ConsoleLog.getLogger(), true)) {
            tripDAO.loadArchive(this::check, db.getConnection());
        }
    }

    @Test
    public void testWrite() throws Exception {
        TripImpl trip = TrackTestTableManager.getTrip(136);
        trip.setTripDescription("new description");
        try (DBHelper db = new DBHelper(ConsoleLog.getLogger(), true)) {
            tripDAO.saveTrip(trip, db.getConnection());
            check(db, trip);
        }
    }

    @Test
    public void testDelete() throws Exception {
        try (DBHelper db = new DBHelper(ConsoleLog.getLogger(), true)) {
            tripDAO.deleteTrip(136, db.getConnection());
            checkDOesNotExist(db, 136);
        }
    }

    private void check(Trip trip) {
        Trip t = TrackTestTableManager.getTrip(trip.getTrip());
        assertEquals(trip.getEndTS(), t.getEndTS());
        assertEquals(trip.getStartTS(), t.getStartTS());
        assertEquals(trip.getDistance(), t.getDistance(), 0.000001);
        assertEquals(trip.getDistanceSail(), t.getDistanceSail(), 0.000001);
        assertEquals(trip.getDistanceMotor(), t.getDistanceMotor(), 0.000001);
    }

    private void checkDOesNotExist(DBHelper h, int id) throws Exception {
        try (PreparedStatement st = h.getConnection().prepareStatement("select * from trip_test where id=?")) {
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            assertFalse(rs.next());
        }
    }

    private void check(DBHelper h, Trip trip) throws Exception {
        try (PreparedStatement st = h.getConnection().prepareStatement("select * from trip_test where id=?")) {
            st.setInt(1, trip.getTrip());
            ResultSet rs = st.executeQuery();
            assertTrue(rs.next());
            assertEquals(trip.getEndTS().toEpochMilli(), rs.getTimestamp("toTS", Utils.UTC_CALENDAR).getTime());
            assertEquals(trip.getStartTS().toEpochMilli(), rs.getTimestamp("fromTS", Utils.UTC_CALENDAR).getTime());
            assertEquals(trip.getTripDescription(), rs.getString("description"));
            assertEquals(trip.getDistance(), rs.getDouble("dist"), 0.000001);
            assertEquals(trip.getDistanceSail(), rs.getDouble("distSail"), 0.000001);
            assertEquals(trip.getDistanceMotor(), rs.getDouble("distMotor"), 0.000001);
        }
    }

}