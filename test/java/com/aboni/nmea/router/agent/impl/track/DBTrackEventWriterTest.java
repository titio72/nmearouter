package com.aboni.nmea.router.agent.impl.track;

import com.aboni.geo.GeoPositionT;
import com.aboni.utils.db.DBHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DBTrackEventWriterTest {

    private DBTrackEventWriter evW;
    private SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Before
    public void setUp() throws Exception {
        TrackTestTableManager.setUp();
        evW = new DBTrackEventWriter();
        evW.sTABLE = "track_test";
    }

    @After
    public void tearDown() throws Exception {
        evW.reset();
        TrackTestTableManager.tearDown();
    }

    @Test
    public void write() throws Exception {
        long l = f.parse("2019-10-15 15:54:12").getTime();
        TrackPoint p = TrackPoint.newInstanceBase(
                new GeoPositionT(l, 43.112234, 9.534534), false, 0.0002, 3.4, 5.4, 30);
        DBHelper h = new DBHelper(true);
        evW.write(new TrackEvent(p), h.getConnection());
        assertTrue(check(h, l, 43.112234, 9.534534, 0.0002, 3.4, 5.4, 30, false, 0));
    }

    @Test
    public void writeWithEngineOn() throws Exception {
        long l = f.parse("2019-10-15 15:54:12").getTime();
        TrackPoint p = TrackPoint.newInstanceWithEngine(
                new GeoPositionT(l, 43.112234, 9.534534), false, 0.0002, 3.4, 5.4, 30, EngineStatus.ON);
        DBHelper h = new DBHelper(true);
        evW.write(new TrackEvent(p), h.getConnection());
        assertTrue(check(h, l, 43.112234, 9.534534, 0.0002, 3.4, 5.4, 30, false, 0, EngineStatus.ON));
    }

    @Test
    public void writeWithTrip() throws Exception {
        long l = f.parse("2019-10-15 15:54:12").getTime();
        TrackPoint p = TrackPoint.newInstanceWithTrip(
                new GeoPositionT(l, 43.112234, 9.534534), false, 0.0002, 3.4, 5.4, 30, 124);
        DBHelper h = new DBHelper(true);
        evW.write(new TrackEvent(p), h.getConnection());
        assertTrue(check(h, l, 43.112234, 9.534534, 0.0002, 3.4, 5.4, 30, false, 124));
    }

    private boolean check(DBHelper h, long ts, double lat, double lon, double dist, double speed, double maxSpeed, int dTime, boolean anchor, int trip) throws Exception {
        return check(h, ts, lat, lon, dist, speed, maxSpeed, dTime, anchor, trip, EngineStatus.UNKNOWN);
    }

    private boolean check(DBHelper h, long ts, double lat, double lon, double dist, double speed, double maxSpeed, int dTime, boolean anchor, int trip, EngineStatus engine) throws Exception {
        PreparedStatement st = h.getConnection().prepareStatement("select id, TS, lat, lon, dist, speed, maxSpeed, dTime, anchor, engine, tripId from track_test where TS=?");
        st.setTimestamp(1, new Timestamp(ts));
        ResultSet rs = st.executeQuery();
        assertTrue(rs.next());
        assertEquals(ts, rs.getTimestamp(2).getTime());
        assertEquals(lat, rs.getDouble(3), 0.000001);
        assertEquals(lon, rs.getDouble(4), 0.000001);
        assertEquals(dist, rs.getDouble(5), 0.000001);
        assertEquals(speed, rs.getDouble(6), 0.000001);
        assertEquals(maxSpeed, rs.getDouble(7), 0.000001);
        assertEquals(dTime, rs.getInt(8));
        assertEquals(anchor ? 1 : 0, rs.getInt(9));
        assertEquals(engine.toByte(), rs.getByte(10));
        assertEquals(trip, rs.getInt(11));
        return true;
    }
}