package com.aboni.nmea.router.agent.impl.track;

import com.aboni.utils.db.DBHelper;

public class TrackTestTableManager {

    private static String SQL_DROP = "DROP TABLE `track_test`";

    private static String SQL_CREATE = "CREATE TABLE `track_test` (" +
            "`lat` decimal(10,7) DEFAULT NULL," +
            "`lon` decimal(10,7) DEFAULT NULL," +
            "`TS` timestamp NOT NULL," +
            "`id` int(11) NOT NULL AUTO_INCREMENT," +
            "`anchor` int(11) DEFAULT '0'," +
            "`dTime` int(11) DEFAULT NULL," +
            "`dist` decimal(10,8) DEFAULT NULL," +
            "`speed` double(10,2) DEFAULT NULL," +
            "`tripid` int(11) DEFAULT NULL," +
            "`maxSpeed` double(10,2) DEFAULT NULL," +
            " PRIMARY KEY (`id`)," +
            " KEY `track_time` (`TS`)" +
            ");";

    public static void setUp() throws Exception {
        try (DBHelper db = new DBHelper(true)) {
            db.getConnection().createStatement().executeUpdate(SQL_CREATE);
        }
    }

    public static void tearDown() throws Exception {
        try (DBHelper db = new DBHelper(true)) {
            db.getConnection().createStatement().executeUpdate(SQL_DROP);
        }
    }
}
