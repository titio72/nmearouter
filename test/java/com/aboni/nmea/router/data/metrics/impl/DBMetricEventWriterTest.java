package com.aboni.nmea.router.data.metrics.impl;

import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.data.ImmutableSample;
import com.aboni.nmea.router.data.Sample;
import com.aboni.nmea.router.data.StatsEvent;
import com.aboni.nmea.router.utils.ConsoleLog;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.utils.Utils;
import com.google.inject.Guice;
import com.google.inject.Injector;
import junit.framework.TestCase;
import org.junit.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;

public class DBMetricEventWriterTest extends TestCase {

    DBMetricEventWriter evW;
    DBHelper helper;

    public void setUp() throws Exception {
        super.setUp();
        Injector injector = Guice.createInjector(new NMEARouterModule());
        ThingsFactory.setInjector(injector);
        MetricTestTableManager.setUp();
        helper = new DBHelper(ConsoleLog.getLogger(), true);
        evW = new DBMetricEventWriter(MetricTestTableManager.METRIC_TABLE_NAME);
    }

    public void tearDown() throws Exception {
        MetricTestTableManager.tearDown();
        helper.close();
    }

    @Test
    public void testWrite() throws Exception {
        Instant t = Instant.parse("2022-01-07T08:02:00Z");
        Sample s = ImmutableSample.newInstance(t.toEpochMilli(), "TWS", 6.2, 8.2,12.1);
        StatsEvent event = new StatsEvent(s, t.toEpochMilli());
        evW.write(event, helper.getConnection());
        check(helper, t.toEpochMilli(), "TWS", 6.2, 8.2, 12.1);
    }

    private static boolean check(DBHelper h, long ts, String type, double vMin, double v, double vMax) throws Exception {
        PreparedStatement st = h.getConnection().prepareStatement("select id, TS, type, vMin, v, vMax from " + MetricTestTableManager.METRIC_TABLE_NAME + " where TS=? and type=?");
        st.setTimestamp(1, new Timestamp(ts), Utils.UTC_CALENDAR);
        st.setString(2, type);
        ResultSet rs = st.executeQuery();
        assertTrue(rs.next());
        System.out.printf("%f %f %f%n", rs.getDouble(4), rs.getDouble(5), rs.getDouble(6));
        assertEquals(vMin, rs.getDouble(4), 0.000001);
        assertEquals(v, rs.getDouble(5), 0.000001);
        assertEquals(vMax, rs.getDouble(6), 0.000001);
        return true;
    }
}