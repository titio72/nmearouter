package com.aboni.nmea.router.data.track.impl;

import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.utils.db.DBHelper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class TrackTestTableManager {

    private static String SQL_DROP = "DROP TABLE IF EXISTS `track_test`";
    private static String SQL_DROP_TRIP = "DROP TABLE IF EXISTS `trip_test`";

    private static String SQL_CREATE = "CREATE TABLE `track_test` (" +
            "`lat` decimal(10,7) DEFAULT NULL," +
            "`lon` decimal(10,7) DEFAULT NULL," +
            "`TS` timestamp NOT NULL," +
            "`id` int(11) NOT NULL AUTO_INCREMENT," +
            "`anchor` int(11) DEFAULT '0'," +
            "`dTime` int(11) DEFAULT NULL," +
            "`dist` decimal(10,8) DEFAULT NULL," +
            "`speed` double(10,2) DEFAULT NULL," +
            "`maxSpeed` double(10,2) DEFAULT NULL," +
            "`engine` TINYINT DEFAULT 2," +
            " PRIMARY KEY (`id`)," +
            " KEY `track_time` (`TS`)" +
            ");";

    private static String SQL_CREATE_TRIP = "CREATE TABLE `trip_test` (" +
            "`id` int(11) NOT NULL," +
            "`description` varchar(256) DEFAULT ''," +
            "`fromTS` timestamp NULL DEFAULT NULL," +
            "`toTS` timestamp NULL DEFAULT NULL," +
            "`dist` decimal(16,8) DEFAULT 0," +
            " PRIMARY KEY (`id`)" +
            ");";

    public static void setUp() throws Exception {
        try (DBHelper db = new DBHelper(true)) {
            db.getConnection().createStatement().executeUpdate(SQL_DROP);
            db.getConnection().createStatement().executeUpdate(SQL_CREATE);
            db.getConnection().createStatement().executeUpdate(SQL_DROP_TRIP);
            db.getConnection().createStatement().executeUpdate(SQL_CREATE_TRIP);
        }
    }

    public static void tearDown() throws Exception {
        try (DBHelper db = new DBHelper(true)) {
            db.getConnection().createStatement().executeUpdate(SQL_DROP);
            db.getConnection().createStatement().executeUpdate(SQL_DROP_TRIP);
        }
    }

    public static Object[][] testTrips = {
            {129, "Meloria - prova jib", "2019-12-29T09:37:49Z", "2019-12-29T18:22:47Z", 26.31941256},
            {130, "Prova staz. vento", "2019-12-30T14:38:10Z", "2019-12-30T16:32:04Z", 3.60648988},
            {131, "Capraia", "2020-01-03T06:43:03Z", "2020-01-05T15:54:38Z", 117.34678943},
            {132, "Bacherotti (ancora)", "2020-01-10T12:37:01Z", "2020-01-10T17:37:36Z", 5.14341488},
            {133, "San Vincenzo", "2020-01-11T06:59:28Z", "2020-01-12T16:12:02Z", 83.13558142},
            {134, "Marina", "2020-01-26T12:17:44Z", "2020-01-26T17:54:44Z", 6.78992152},
            {135, "Capraia", "2020-02-08T06:49:58Z", "2020-02-09T16:21:37Z", 87.07638543},
            {136, "Capraia", "2020-03-07T07:52:01Z", "2020-03-08T17:21:59Z", 90.40208639}};

    public static TripImpl getTrip(Object[] o) {
        TripImpl t = new TripImpl((Integer) o[0], (String) o[1]);
        t.setDistance((Double) o[4]);
        t.setTS(Instant.parse((String) o[2]));
        t.setTS(Instant.parse((String) o[3]));
        return t;
    }

    public static TripImpl getTrip(int i) {
        for (Object[] t : testTrips) {
            if (i == (Integer) t[0]) {
                return getTrip(t);
            }
        }
        return null;
    }

    public static void addTestData() throws SQLException, MalformedConfigurationException, ClassNotFoundException {
        try (DBHelper db = new DBHelper(false)) {
            PreparedStatement st = db.getConnection().prepareStatement("insert into trip_test values (?, ?, ?, ?, ?)");
            for (Object[] t : testTrips) {
                st.setInt(1, (Integer) t[0]);
                st.setString(2, (String) t[1]);
                st.setTimestamp(3, new Timestamp(Instant.parse((String) t[2]).toEpochMilli()));
                st.setTimestamp(4, new Timestamp(Instant.parse((String) t[3]).toEpochMilli()));
                st.setDouble(5, (Double) t[4]);
                assert (st.executeUpdate() == 1);
            }
            db.getConnection().commit();
        }
    }
}
