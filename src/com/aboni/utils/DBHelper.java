package com.aboni.utils;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
            File f = new File("db.properties");
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
    
}
