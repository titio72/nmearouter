package com.aboni.utils.db;

import com.aboni.utils.Constants;
import com.aboni.utils.GDrive;
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
    private String dburl = DB_URL;
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
				dburl = p.getProperty("jdbc.url");
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
		}
    }
    
    private boolean reconnect() {
    	try {
            close();
            ServerLog.getLogger().debug("Establishing connection to DB {" + dburl + "}!");
            conn = DriverManager.getConnection(dburl, user, password);
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
        Process proc = b.start();
        int retCode = proc.waitFor();
        if (retCode==0) {
            upload("./web/" + file + ".tgz");
            return file;
        } else {
        	return null;
        }
    }

    private Thread gDriveThread;

    private void upload(String file) {
        if (gDriveThread==null) {
            gDriveThread = new Thread(() -> {
                try {
                    GDrive.upload(file, "application/x-gtar");
                } catch (Exception e) {
                    ServerLog.getLogger().error("Error uploading backup", e);
                }
            }
            );
            gDriveThread.setDaemon(true);
        } else if (gDriveThread.isAlive()) {
            gDriveThread.interrupt();
        }
        gDriveThread.start();
    }

    public boolean write(EventWriter writer, Event e) {
    	return write(writer, e, 0);
    }

    private boolean write(EventWriter writer, Event e, int count) {
    	boolean retry = false;
    	if (writer!=null && e!=null) {
            try {
                writer.write(e, getConnection());
                return true;
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
    	return false;
    }
}
