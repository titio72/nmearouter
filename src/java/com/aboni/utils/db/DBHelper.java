/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.utils.db;

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import com.aboni.utils.ThingsFactory;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class DBHelper implements AutoCloseable {

    private static InfluxDB influx = null;
    private static final Object influxSemaphore = new Object();

    private static final String INFLUX_URL = "http://localhost:8086";
    private static final String INFLUX_DB = "nmearouter";

    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost/nmearouter";
    private static final String DEFAULT_USER = "user";
    public static final String DB_HELPER_CATEGORY = "DBHelper";

    private static String influxURL = INFLUX_URL;
    private static String influxDB = INFLUX_DB;

    private String jdbc = JDBC_DRIVER;
    private String dbUrl = DB_URL;
    private String user = DEFAULT_USER;
    private String password;

    private final boolean autocommit;
    private Connection conn;

    private final Log log;

    private static InfluxDB getInfluxStatic() {
        synchronized (influxSemaphore) {
            if (influx == null) {
                influx = InfluxDBFactory.connect(influxURL);
                influx.setDatabase(influxDB);
                influx.enableBatch(2000, 10000, TimeUnit.MILLISECONDS);
            }
        }
        return influx;
    }

    public DBHelper(boolean autocommit) throws ClassNotFoundException, MalformedConfigurationException {
        readConf();
        this.autocommit = autocommit;
        Class.forName(jdbc);
        log = ThingsFactory.getInstance(Log.class);
        reconnect();
    }

    private void readConf() throws MalformedConfigurationException {
        try {
            File f = new File(Constants.DB);
            try (FileInputStream propInput = new FileInputStream(f)) {
                Properties p = new Properties();
                p.load(propInput);
                jdbc = p.getProperty("jdbc.driver.class", JDBC_DRIVER);
                dbUrl = p.getProperty("jdbc.url", DB_URL);
                user = p.getProperty("user");
                password = p.getProperty("pwd");

                synchronized (influxSemaphore) {
                    influxURL = p.getProperty("influx.url");
                    influxDB = p.getProperty("influx.db");
                }
            }
        } catch (Exception e) {
            log.debug(LogStringBuilder.start(DB_HELPER_CATEGORY).wO("Read configuration")
                    .wV("error", e.getMessage()).toString());
            throw new MalformedConfigurationException("Cannot read DB configuration", e);
        }
    }

    public Connection getConnection() {
        return conn;
    }

    @Override
    public void close() {
        if (conn != null) {
            try {
                log.debug(() -> LogStringBuilder.start(DB_HELPER_CATEGORY).wO("Close").toString());
                conn.close();
            } catch (SQLException e) {
                log.error(LogStringBuilder.start(DB_HELPER_CATEGORY).wO("Close").toString(), e);
            }
            conn = null;
        }
    }

    private boolean reconnect() {
        try {
            close();
            log.debug(() -> LogStringBuilder.start(DB_HELPER_CATEGORY).wO("Connect").toString());
            conn = DriverManager.getConnection(dbUrl, user, password);
            conn.setAutoCommit(autocommit);
            return true;
        } catch (Exception e) {
            conn = null;
            log.error(LogStringBuilder.start(DB_HELPER_CATEGORY).wO("Connect").toString(), e);
            return false;
        }
    }

    public synchronized String backup() throws IOException, InterruptedException {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        log.info("DB Backup");
        String file = df.format(new Date()) + ".sql";
        ProcessBuilder b = new ProcessBuilder("./dbBck.sh", user, password, file);
        Process process = b.start();
        int retCode = process.waitFor();
        if (retCode == 0) {
            return file;
        } else {
            return null;
        }
    }

    public void write(DBEventWriter writer, Event e) {
        write(writer, e, 0);
    }

    private void write(DBEventWriter writer, Event e, int count) {
        boolean retry = false;
        if (writer != null && e != null) {
            try {
                writer.write(e, getConnection());
            } catch (Exception ex) {
                resetWriter(writer);
                retry = true;
                log.error(LogStringBuilder.start(DB_HELPER_CATEGORY).wO("Write event")
                        .wV("event", e).wV("count", count).toString(), ex);
            }
        }
        if (retry) {
            count++;
            if (count < 3 && reconnect()) {
                write(writer, e, count);
            }
        }
    }

    public InfluxDB getInflux() {
        return getInfluxStatic();
    }

    private void resetWriter(DBEventWriter writer) {
        try {
            writer.reset();
        } catch (SQLException ignored) {
            // do nothing
        }
    }

    public void writeMetric(long time, String table, String metric, double value, double valueMin, double valueMax) {
        InfluxDB influxDB = getInfluxStatic();
        if (influxDB != null) {
            influx.write(Point.measurement(table)
                    .time(time, TimeUnit.MILLISECONDS)
                    .tag("type", metric)
                    .addField("value", value)
                    .addField("max", valueMax)
                    .addField("min", valueMin)
                    .build());
        }
    }

    public void writeMetric(long time, String table, String metric, double value) {
        InfluxDB influxDB = getInfluxStatic();
        if (influxDB != null) {
            influx.write(Point.measurement(table)
                    .time(time, TimeUnit.MILLISECONDS)
                    .tag("type", metric)
                    .addField("value", value)
                    .build());
        }
    }
}
