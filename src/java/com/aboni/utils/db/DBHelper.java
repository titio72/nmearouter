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
import com.aboni.utils.ServerLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class DBHelper implements AutoCloseable {

    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost/nmearouter";
    private static final String DEFAULT_USER = "user";

    private String jdbc = JDBC_DRIVER;
    private String dbUrl = DB_URL;
    private String user = DEFAULT_USER;
    private String password;

    private final boolean autocommit;
    private Connection conn;

    public DBHelper(boolean autocommit) throws ClassNotFoundException {
        readConf();
        this.autocommit = autocommit;
        Class.forName(jdbc);
        reconnect();
    }

    private void readConf() {
        try {
            File f = new File(Constants.DB);
            try (FileInputStream propInput = new FileInputStream(f)) {
                Properties p = new Properties();
                p.load(propInput);
                jdbc = p.getProperty("jdbc.driver.class");
                dbUrl = p.getProperty("jdbc.url");
                user = p.getProperty("user");
                password = p.getProperty("pwd");
            }
        } catch (Exception e) {
            ServerLog.getLogger().debug("Cannot read db configuration!");
        }
    }

    public Connection getConnection() {
        return conn;
    }

    @Override
    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                ServerLog.getLogger().error("Error closing connection!", e);
            }
            conn = null;
        }
    }

    private boolean reconnect() {
        try {
            close();
            ServerLog.getLogger().debug("Establishing connection to DB {" + dbUrl + "}!");
            conn = DriverManager.getConnection(dbUrl, user, password);
            conn.setAutoCommit(autocommit);
            return true;
        } catch (Exception e) {
            conn = null;
            ServerLog.getLogger().error("Cannot reset connection!", e);
            return false;
        }
    }

    public synchronized String backup() throws IOException, InterruptedException {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        ServerLog.getLogger().info("DB Backup");
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
                writer.reset();
                retry = true;
                ServerLog.getLogger().error("Cannot write {" + e + "} (" + count + ")!", ex);
            }
        }
        if (retry) {
            count++;
            if (count<3 && reconnect()) {
                write(writer, e, count);
            }
        }
    }
}
