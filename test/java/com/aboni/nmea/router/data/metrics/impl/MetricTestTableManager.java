package com.aboni.nmea.router.data.metrics.impl;

import com.aboni.log.ConsoleLog;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.utils.Utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

public class MetricTestTableManager {

    public static final String METRIC_TABLE_NAME = "meteo_test";

    private static final String SQL_DROP = "DROP TABLE IF EXISTS `" + METRIC_TABLE_NAME + "`";

    private static final String SQL_CREATE = "CREATE TABLE `" + METRIC_TABLE_NAME + "` (" +
            "`id` int(11) NOT NULL AUTO_INCREMENT," +
            "`type` char(3) NOT NULL," +
            "`TS` datetime NOT NULL," +
            "`vMin` double DEFAULT NULL," +
            "`v` double NOT NULL," +
            "`vMax` double DEFAULT NULL," +
            " PRIMARY KEY (`id`)," +
            " KEY `time` (`TS`)" +
            ");";


    public static void setUp() throws Exception {
        try (DBHelper db = new DBHelper(ConsoleLog.getLogger(), true)) {
            db.getConnection().createStatement().executeUpdate(SQL_DROP);
            db.getConnection().createStatement().executeUpdate(SQL_CREATE);
        }
    }

    public static void tearDown() throws Exception {
        try (DBHelper db = new DBHelper(ConsoleLog.getLogger(), true)) {
            db.getConnection().createStatement().executeUpdate(SQL_DROP);
        }
    }

    private static void privateLoadCSVLine(PreparedStatement st, String l) throws SQLException {
        String[] csv = l.split(",");
        Instant d = Instant.parse(csv[1]);
        st.setTimestamp(1, new Timestamp(d.toEpochMilli()), Utils.UTC_CALENDAR);
        st.setString(2, csv[0]);
        st.setDouble(3, Double.parseDouble(csv[2]));
        st.setDouble(4, Double.parseDouble(csv[3]));
        st.setDouble(5, Double.parseDouble(csv[4]));
    }

    /**
     * Load a csv to populate the test DB
     * Each line is in the form "TWD,2022-12-18T00:47:51Z,88.33,105.83,126.26"
     *
     * @param csv An array of strings, each represent a record.
     * @throws SQLException
     * @throws MalformedConfigurationException
     */
    public static void loadTrackCSV(String[] csv) throws SQLException, MalformedConfigurationException {
        try (DBHelper db = new DBHelper(ConsoleLog.getLogger(), false)) {
            PreparedStatement st = db.getConnection().prepareStatement("insert into " + METRIC_TABLE_NAME +
                    " (TS, type, vMin, v, vMax) values (?, ?, ?, ?, ?)");
            for (String l : csv) {
                privateLoadCSVLine(st, l);
                assert (st.executeUpdate() == 1);
            }
            db.getConnection().commit();
        }
    }
}
