package com.aboni.nmea.router.data.track.impl;

import com.aboni.nmea.router.data.track.TripEvent;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.utils.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DBTripEventWriterTest {

    private DBTripEventWriter evW;

    @Before
    public void setUp() throws Exception {
        TrackTestTableManager.setUp();
        TrackTestTableManager.addTestData();
        evW = new DBTripEventWriter("trip_test");
    }

    @After
    public void tearDown() throws Exception {
        evW.reset();
        TrackTestTableManager.tearDown();
    }

    @Test
    public void write() throws Exception {
        TripImpl trip = TrackTestTableManager.getTrip(136);
        trip.setTS(trip.getEndTS().plusSeconds(30));
        trip.addDistance(0.0543);
        try (DBHelper db = new DBHelper(true)) {
            evW.write(new TripEvent(trip), db.getConnection());
            check(db, 136, trip.getEndTS().toEpochMilli(), trip.getDistance());
        }
    }

    private boolean check(DBHelper h, int id, long endTS, double dist) throws Exception {
        PreparedStatement st = h.getConnection().prepareStatement("select * from trip_test where id=?");
        st.setInt(1, id);
        ResultSet rs = st.executeQuery();
        assertTrue(rs.next());
        assertEquals(endTS, rs.getTimestamp("toTS", Utils.UTC_CALENDAR).getTime());
        assertEquals(dist, rs.getDouble("dist"), 0.000001);
        return true;
    }

}