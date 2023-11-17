package com.aboni.nmea.router.data.track.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.NMEAMessagesModule;
import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.data.track.TrackEvent;
import com.aboni.nmea.router.data.track.TrackPoint;
import com.aboni.log.ConsoleLog;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.sensors.EngineStatus;
import com.aboni.utils.Utils;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TrackDAOTest {

    private TrackDAO evW;

    @Before
    public void setUp() throws Exception {
        Injector injector = Guice.createInjector(new NMEARouterModule(), new NMEAMessagesModule());
        ThingsFactory.setInjector(injector);
        TrackTestTableManager.setUp();
        evW = new TrackDAO("track_test");
    }

    @After
    public void tearDown() throws Exception {
        TrackTestTableManager.tearDown();
    }

    @Test
    public void write() throws Exception {
        long l = Instant.parse("2019-10-15T15:54:12Z").toEpochMilli();
        TrackPoint p = new TrackPointBuilderImpl().
                withPosition(new GeoPositionT(l, 43.112234, 9.534534))
                .withDistance(0.0002)
                .withSpeed(3.4, 5.4)
                .withPeriod(30).withEngine(EngineStatus.OFF).getPoint();
        DBHelper h = new DBHelper(ConsoleLog.getLogger(), true);
        evW.writeEvent(new TrackEvent(p), h.getConnection());
        assertTrue(check(h, l, 43.112234, 9.534534, 0.0002, 3.4, 5.4, 30, false, EngineStatus.OFF));
    }

    @Test
    public void writeWithEngineOn() throws Exception {
        long l = Instant.parse("2019-10-15T15:54:12Z").toEpochMilli();
        TrackPoint p = new TrackPointBuilderImpl()
                .withPosition(new GeoPositionT(l, 43.112234, 9.534534))
                .withDistance(0.0002)
                .withSpeed(3.4, 5.4).withPeriod(30).withEngine(EngineStatus.ON).getPoint();
        DBHelper h = new DBHelper(ConsoleLog.getLogger(), true);
        evW.writeEvent(new TrackEvent(p), h.getConnection());
        assertTrue(check(h, l, 43.112234, 9.534534, 0.0002, 3.4, 5.4, 30, false, EngineStatus.ON));
    }

    private boolean check(DBHelper h, long ts, double lat, double lon, double dist, double speed, double maxSpeed, int dTime, boolean anchor, EngineStatus engine) throws Exception {
        PreparedStatement st = h.getConnection().prepareStatement("select id, TS, lat, lon, dist, speed, maxSpeed, dTime, anchor, engine from track_test where TS=?");
        st.setTimestamp(1, new Timestamp(ts), Utils.UTC_CALENDAR);
        ResultSet rs = st.executeQuery();
        assertTrue(rs.next());
        assertEquals(ts, rs.getTimestamp(2, Utils.UTC_CALENDAR).getTime());
        assertEquals(lat, rs.getDouble(3), 0.000001);
        assertEquals(lon, rs.getDouble(4), 0.000001);
        assertEquals(dist, rs.getDouble(5), 0.000001);
        assertEquals(speed, rs.getDouble(6), 0.000001);
        assertEquals(maxSpeed, rs.getDouble(7), 0.000001);
        assertEquals(dTime, rs.getInt(8));
        assertEquals(anchor ? 1 : 0, rs.getInt(9));
        assertEquals(engine.toByte(), rs.getByte(10));
        return true;
    }
}