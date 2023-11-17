package com.aboni.nmea.router.data.track.impl;

import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.data.track.impl.TripImpl;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.log.ConsoleLog;
import com.aboni.utils.Utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class TrackTestTableManager {

    public static final String TRACK_TABLE_NAME = "track_test";
    public static final String TRIP_TABLE_NAME = "trip_test";

    private static final String SQL_DROP = "DROP TABLE IF EXISTS `" + TRACK_TABLE_NAME + "`";
    private static final String SQL_DROP_TRIP = "DROP TABLE IF EXISTS `" + TRIP_TABLE_NAME + "`";

    private static final String SQL_CREATE = "CREATE TABLE `" + TRACK_TABLE_NAME + "` (" +
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

    private static final String SQL_CREATE_TRIP = "CREATE TABLE `" + TRIP_TABLE_NAME + "` (" +
            "`id` int(11) NOT NULL," +
            "`description` varchar(256) DEFAULT ''," +
            "`fromTS` timestamp NULL DEFAULT NULL," +
            "`toTS` timestamp NULL DEFAULT NULL," +
            "`dist` decimal(16,8) DEFAULT 0," +
            "`distSail` decimal(16,8) DEFAULT 0," +
            "`distMotor` decimal(16,8) DEFAULT 0," +
            " PRIMARY KEY (`id`)" +
            ");";

    public static void setUp() throws Exception {
        try (DBHelper db = new DBHelper(ConsoleLog.getLogger(), true)) {
            db.getConnection().createStatement().executeUpdate(SQL_DROP);
            db.getConnection().createStatement().executeUpdate(SQL_CREATE);
            db.getConnection().createStatement().executeUpdate(SQL_DROP_TRIP);
            db.getConnection().createStatement().executeUpdate(SQL_CREATE_TRIP);
        }
    }

    public static void tearDown() throws Exception {
        try (DBHelper db = new DBHelper(ConsoleLog.getLogger(), true)) {
            db.getConnection().createStatement().executeUpdate(SQL_DROP);
            db.getConnection().createStatement().executeUpdate(SQL_DROP_TRIP);
        }
    }

    public static Object[][] testTrips = {
            {131, "Capraia",                "2020-01-03T05:43:03Z", "2020-01-05T14:54:38Z", 117.34678943, 93.81118865, 23.45251967},
            {132, "Bacherotti (ancora)",    "2020-01-10T11:37:01Z", "2020-01-10T16:37:36Z",   5.14341488,  0.02232004,  5.11249466},
            {133, "San Vincenzo",           "2020-01-11T05:59:28Z", "2020-01-12T15:12:02Z",  83.13558142, 68.25720536, 14.83606047},
            {134, "Marina",                 "2020-01-26T11:17:44Z", "2020-01-26T16:54:44Z",   6.78992152,  3.19080244,  3.59431515},
            {135, "Capraia",                "2020-02-08T05:49:58Z", "2020-02-09T15:21:37Z",  87.07638543, 84.63910278,  2.43728265},
            {136, "Capraia",                "2020-03-07T06:52:01Z", "2020-03-08T16:21:59Z",  90.40208639, 52.53053940, 37.87154699}
    };

    public static TripImpl getTrip(Object[] o) {
        TripImpl t = new TripImpl((Integer) o[0], (String) o[1]);
        t.setDistance((Double) o[4]);
        t.setDistanceSail((Double) o[5]);
        t.setDistanceMotor((Double) o[6]);
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
        try (DBHelper db = new DBHelper(ConsoleLog.getLogger(), false)) {
            try (PreparedStatement st = db.getConnection().prepareStatement("insert into " + TRIP_TABLE_NAME + " values (?, ?, ?, ?, ?, ?, ?)")) {
                for (Object[] t : testTrips) {
                    st.setInt(1, (Integer) t[0]);
                    st.setString(2, (String) t[1]);
                    st.setTimestamp(3, new Timestamp(Instant.parse((String) t[2]).toEpochMilli()), Utils.UTC_CALENDAR);
                    st.setTimestamp(4, new Timestamp(Instant.parse((String) t[3]).toEpochMilli()), Utils.UTC_CALENDAR);
                    st.setDouble(5, (Double) t[4]);
                    st.setDouble(6, (Double) t[5]);
                    st.setDouble(7, (Double) t[6]);
                    assert (st.executeUpdate() == 1);
                }
            }
            db.getConnection().commit();
        }
    }

    /*
            "`lat` decimal(10,7) DEFAULT NULL," +
            "`lon` decimal(10,7) DEFAULT NULL," +
            "`TS` timestamp NOT NULL," +
            "`anchor` int(11) DEFAULT '0'," +
            "`dTime` int(11) DEFAULT NULL," +
            "`dist` decimal(10,8) DEFAULT NULL," +
            "`speed` double(10,2) DEFAULT NULL," +
            "`maxSpeed` double(10,2) DEFAULT NULL," +
            "`engine` TINYINT DEFAULT 2," +

     */

    private static void privateLoadCSVLine(PreparedStatement st, String l) throws SQLException {
        String[] csv = l.split(",");
        st.setDouble(1, Double.parseDouble(csv[0]));
        st.setDouble(2, Double.parseDouble(csv[1]));
        Instant d = Instant.parse(csv[2]);
        st.setTimestamp(3, new Timestamp(d.toEpochMilli()), Utils.UTC_CALENDAR);
        st.setInt(4, Integer.parseInt(csv[4]));
        st.setInt(5, Integer.parseInt(csv[5]));
        st.setDouble(6, Double.parseDouble(csv[6]));
        st.setDouble(7, Double.parseDouble(csv[7]));
        st.setDouble(8, Double.parseDouble(csv[8]));
        st.setDouble(9, Integer.parseInt(csv[9]));
    }

    public static void loadTrackCSV(String[] csv) throws SQLException, MalformedConfigurationException, ClassNotFoundException {
        try (DBHelper db = new DBHelper(ConsoleLog.getLogger(), false)) {
            PreparedStatement st = db.getConnection().prepareStatement("insert into " + TRACK_TABLE_NAME +
                    " (lat, lon, TS, anchor, dTime, dist, speed, maxSpeed, engine) values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            for (String l : csv) {
                privateLoadCSVLine(st, l);
                assert (st.executeUpdate() == 1);
            }
            db.getConnection().commit();
        }
    }
}
