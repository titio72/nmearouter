/*
 * Copyright (c) 2022,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

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

package com.aboni.nmea.router.utils.db;

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.log.Log;
import com.aboni.log.LogStringBuilder;
import com.aboni.log.SafeLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class DBHelper implements AutoCloseable {

    private static final String DB_URL = "jdbc:mysql://localhost/nmearouter";
    private static final String DEFAULT_USER = "user";
    public static final String DB_HELPER_CATEGORY = "DBHelper";

    private String dbUrl = DB_URL;
    private String user = DEFAULT_USER;
    private String password;

    private final boolean autocommit;
    private Connection conn;

    private final Log log;

    public DBHelper(Log log, boolean autocommit) throws MalformedConfigurationException {
        readConf();
        this.autocommit = autocommit;
        this.log = SafeLog.getSafeLog(log);
        reconnect();
    }

    private void readConf() throws MalformedConfigurationException {
        File f = new File(Constants.DB);
        try (FileInputStream propInput = new FileInputStream(f)) {
            Properties p = new Properties();
            p.load(propInput);
            dbUrl = p.getProperty("jdbc.url", DB_URL);
            user = p.getProperty("user");
            password = p.getProperty("pwd");
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

    private void resetWriter(DBEventWriter writer) {
        try {
            writer.reset();
        } catch (SQLException ignored) {
            // do nothing
        }
    }

    private PreparedStatement prepareNewStatement(String query, QueryPreparer preparer) throws SQLException {
        PreparedStatement p = getConnection().prepareStatement(query);
        preparer.prepare(p);
        return p;
    }

    public <T extends Exception> void executeQuery(String query, QueryReader<T> reader) throws SQLException, T {
        if (query==null) throw new IllegalArgumentException("Query is null");
        try (
                Statement statement = getConnection().createStatement();
                ResultSet rs = statement.executeQuery(query)
        ) {
            if (reader != null) reader.readQuery(rs);
        }
    }

    public <T extends Exception> void executeQuery(String query, QueryPreparer preparer, QueryReader<T> reader) throws SQLException, T {
        if (query==null) throw new IllegalArgumentException("Query is null");
        try (
            PreparedStatement statement = prepareNewStatement(query, preparer);
            ResultSet rs = statement.executeQuery()
        ) {
            if (reader!=null) reader.readQuery(rs);
        }
    }

    public interface QueryPreparer {
        void prepare(PreparedStatement statement) throws SQLException;
    }

    public interface QueryReader<T extends Exception> {
        void readQuery(ResultSet reader) throws SQLException, T;
    }
}
