package com.aboni.nmea.router.agent.impl.track;

import com.aboni.utils.Pair;
import com.aboni.utils.db.DBHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DBTripManagerTest {
    private String[] SQL_CREATE_ROWS = new String[]{
            "insert into track_test values (43.9597500, 9.7747000, '2019-10-15 19:20:31', 290434, 0,  9, 0.01249447, 5.00, 124, 5.40);",
            "insert into track_test values (43.9589667, 9.7757500, '2019-10-15 19:21:16', 290435, 0, 45, 0.06541637, 5.23, 124, 5.50);",
            "insert into track_test values (43.9584500, 9.7764167, '2019-10-15 19:21:46', 290436, 0, 30, 0.04237447, 5.08, NULL,  5.50);",
            "insert into track_test values (43.9579167, 9.7771167, '2019-10-15 19:22:16', 290437, 0, 30, 0.04409267, 5.29, NULL,  5.50);",
            "insert into track_test values (43.9573833, 9.7778000, '2019-10-15 19:22:46', 290438, 0, 30, 0.04359903, 5.23, NULL,  5.50);",
            "insert into track_test values (43.9568667, 9.7785000, '2019-10-15 19:23:16', 290439, 0, 30, 0.04337287, 5.20, NULL,  5.50);",
            "insert into track_test values (43.9563333, 9.7792000, '2019-10-15 19:23:46', 290440, 0, 30, 0.04409322, 5.29, NULL,  5.50);",
            "insert into track_test values (43.9558000, 9.7799000, '2019-10-15 19:24:16', 290441, 0, 30, 0.04409340, 5.29, NULL,  5.50);",
            "insert into track_test values (43.9552667, 9.7806000, '2019-10-15 19:24:46', 290442, 0, 30, 0.04409359, 5.29, NULL,  5.50);",
            "insert into track_test values (43.9547333, 9.7813000, '2019-10-15 19:25:16', 290443, 0, 30, 0.04409377, 5.29, NULL,  5.50);",
            "insert into track_test values (43.9542167, 9.7819833, '2019-10-15 19:25:46', 290444, 0, 30, 0.04287164, 5.14, NULL,  5.50);",
            "insert into track_test values (43.9536833, 9.7826667, '2019-10-15 19:26:16', 290445, 0, 30, 0.04360027, 5.23, NULL,  5.50);",
            "insert into track_test values (43.9531667, 9.7833667, '2019-10-15 19:26:46', 290446, 0, 30, 0.04337417, 5.20, NULL,  5.50);",
            "insert into track_test values (43.9524833, 9.7842500, '2019-10-15 19:27:25', 290447, 0, 39, 0.05609389, 5.18, NULL,  5.40);",
            "insert into track_test values (43.9519500, 9.7849667, '2019-10-15 19:27:55', 290448, 0, 30, 0.04459485, 5.35, NULL,  5.50);",
            "insert into track_test values (43.9512000, 9.7859333, '2019-10-15 19:28:36', 290449, 0, 41, 0.06148294, 5.40, NULL,  5.50);",
            "insert into track_test values (43.9506667, 9.7866167, '2019-10-15 19:29:06', 290450, 0, 30, 0.04360127, 5.23, NULL,  5.50);",
            "insert into track_test values (43.9501500, 9.7873167, '2019-10-15 19:29:36', 290451, 0, 30, 0.04337523, 5.21, NULL,  5.50);"
    };

    private DBTripManager m;

    @Before
    public void setUp() throws Exception {

        TrackTestTableManager.setUp();

        m = new DBTripManager();
        m.sTABLE = "track_test";
        try (DBHelper db = new DBHelper(true)) {
            for (String sql : SQL_CREATE_ROWS) db.getConnection().createStatement().executeUpdate(sql);
        }
    }

    @After
    public void tearDown() throws Exception {
        TrackTestTableManager.tearDown();
    }

    @Test
    public void getCurrentTrip() throws TripManagerException, ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-10-15 20:10:00");
        Pair<Integer, Long> p = m.getCurrentTrip(d.getTime());
        assertNotNull(p);
        assertEquals(124, (int) p.first);
        assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-10-15 19:21:16").getTime(), (long) p.second);
    }

    @Test
    public void setTrip() throws TripManagerException, ParseException, SQLException, ClassNotFoundException {
        m.setTrip(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-10-15 19:21:16").getTime(),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2019-10-15 20:10:00").getTime(), 124);

        try (DBHelper db = new DBHelper(true)) {
            ResultSet rs = db.getConnection().createStatement().executeQuery("select id, tripId from track_test where id>=290436");
            int c = 0;
            while (rs.next()) {
                assertEquals(124, rs.getInt(2));
                c++;
            }
            assertEquals(16, c);

        }
    }
}