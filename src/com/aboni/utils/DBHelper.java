package com.aboni.utils;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
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

    private Connection conn;

    protected void readConf() {
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
    
    
    public DBHelper(boolean autocommit) throws ClassNotFoundException, SQLException {
    	readConf();
    	
        Class.forName(jdbc);
        conn = DriverManager.getConnection(dburl, user, password);
        conn.setAutoCommit(autocommit);
    }
    
    public Connection getConnection() {
        return conn;
    }
    
    public void close() throws SQLException {
        conn.close();
    }
    
    public Timestamp[] getTimeframe(String table, Calendar cFrom, Calendar cTo) throws SQLException {
        PreparedStatement stm = getConnection().prepareStatement("select max(TS), min(TS) from " + table + " where TS>=? and TS<=?");
    	stm.setTimestamp(1, new java.sql.Timestamp(cFrom.getTimeInMillis() ));
    	stm.setTimestamp(2, new java.sql.Timestamp(cTo.getTimeInMillis() ));
        ResultSet rs = stm.executeQuery();
        if (rs.next()) 
        	return new Timestamp[] { rs.getTimestamp(1), rs.getTimestamp(2) };
        else 
        	return null;
    }
    
}
