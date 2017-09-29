package com.aboni.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class DBHelper {
	
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    private static final String DB_URL = "jdbc:mysql://localhost/nmearouter";
    private static final String USER = "user";
    private static final String PASS = "pwd";

    private String jdbc = JDBC_DRIVER;  
    private String dburl = DB_URL;
    private String user = USER;
    private String password = PASS;

    private boolean autocommit;
    private Connection conn;
    
    public DBHelper(boolean autocommit) throws ClassNotFoundException, SQLException {
    	readConf();
        this.autocommit = autocommit;
    	
        Class.forName(jdbc);
        conn = DriverManager.getConnection(dburl, user, password);
        conn.setAutoCommit(autocommit);
    }
    
    protected final void readConf() {
        try {
            File f = new File(Constants.DB);
            FileInputStream propInput = new FileInputStream(f);
            Properties p = new Properties();
            p.load(propInput);
            propInput.close();
            
            jdbc = p.getProperty("jdbc.driver.class");
            dburl = p.getProperty("jdbc.url");
            user = p.getProperty("user");
            password = p.getProperty("pwd");

        } catch (Exception e) {
            ServerLog.getLogger().Debug("Cannot read db configuration!");
        }
    }
    
    public Connection getConnection() {
        return conn;
    }
    
    public void close() throws SQLException {
        if (conn!=null) conn.close();
    }
    
    public void reconnect() throws SQLException {
    	close();
        conn = DriverManager.getConnection(dburl, user, password);
        conn.setAutoCommit(autocommit);
    }
    
    public synchronized PreparedStatement getTimeSeries(String table, String[] fields, Calendar cFrom, Calendar cTo, String where) throws SQLException {
	    String sql = "select TS ";
	    for (String f: fields) {
	    	sql += ", " + f;
	    }
    	sql += " from " + table + " where TS>=? and TS<=?";
    	if (where!=null) {
    		sql += " AND " + where;
    	}
    	PreparedStatement stm = getConnection().prepareStatement(sql);
		stm.setTimestamp(1, new java.sql.Timestamp(cFrom.getTimeInMillis() ));
		stm.setTimestamp(2, new java.sql.Timestamp(cTo.getTimeInMillis() ));
		return stm;
    }    
    
    public class Range {
    	private Timestamp max;
    	private Timestamp min;
    	private long count;
    	
    	public Range(Timestamp max, Timestamp min, long count) {
    		this.max = max;
    		this.min = min;
    		this.count = count;
    	}

		public Timestamp getMax() {
			return max;
		}

		public Timestamp getMin() {
			return min;
		}
		
		public long getCount() {
			return count;
		}
		
		public long getInterval() {
			return max.getTime() - min.getTime();
		}
		
		public int getSampling(int maxSamples) {
            return (int) ((getCount()<=maxSamples)?1:(getInterval()/maxSamples));
			
		}
    }
    
    public synchronized Range getTimeframe(String table, Calendar cFrom, Calendar cTo) throws SQLException {
        PreparedStatement stm = getConnection().prepareStatement("select count(TS), max(TS), min(TS) from " + table + " where TS>=? and TS<=?");
    	stm.setTimestamp(1, new java.sql.Timestamp(cFrom.getTimeInMillis() ));
    	stm.setTimestamp(2, new java.sql.Timestamp(cTo.getTimeInMillis() ));
        ResultSet rs = stm.executeQuery();
        if (rs.next()) {
        	long count = rs.getLong(1);
        	Timestamp tMax = rs.getTimestamp(2);
        	Timestamp tMin = rs.getTimestamp(3);
        	if (tMax!=null && tMin!=null) {
	        	return new Range(tMax, tMin, count);
        	}
        }
        return null;
    }
    
    public synchronized String backup() throws IOException, InterruptedException {
    	SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        ServerLog.getLogger().Info("DB Backup");
        String file = df.format(new Date()) + ".sql";
        ProcessBuilder b = new ProcessBuilder("./dbBck.sh", user, password, file);
        Process proc = b.start();
        int retCode = proc.waitFor();
        if (retCode==0) {
        	return file;
        } else {
        	return null;
        }
    }
}
