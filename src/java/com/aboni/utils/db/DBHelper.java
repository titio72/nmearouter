package com.aboni.utils.db;

import com.aboni.utils.Constants;
import com.aboni.utils.ServerLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class DBHelper implements AutoCloseable {
	
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
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
    		ServerLog.getLogger().info("Establishing connection to DB {" + dburl + "}!");
            conn = DriverManager.getConnection(dburl, user, password);
    		conn.setAutoCommit(autocommit);
    		return true;
    	} catch (Exception e) {
    		conn = null;
            ServerLog.getLogger().error("Cannot reset onnection!", e);
            return false;
    	}
    }
    
    public synchronized PreparedStatement getTimeSeries(String table, String[] fields, Calendar cFrom, Calendar cTo, String where) throws SQLException {
    	if (getConnection()!=null) {
			StringBuilder sqlBuilder = new StringBuilder("select TS ");
			for (String f: fields) {
		    	sqlBuilder.append(", ").append(f);
		    }
			String sql = sqlBuilder.toString();
			sql += " from " + table + " where TS>=? and TS<=?";
	    	if (where!=null) {
	    		sql += " AND " + where;
	    	}
	    	PreparedStatement stm = getConnection().prepareStatement(sql);
			stm.setTimestamp(1, new java.sql.Timestamp(cFrom.getTimeInMillis() ));
			stm.setTimestamp(2, new java.sql.Timestamp(cTo.getTimeInMillis() ));
			return stm;
    	} else {
    		ServerLog.getLogger().warning("Cannot create statement for {" + table + "} because connection is not established!");
    		return null;
    	}
    }    
    
    public class Range {
    	private final Timestamp max;
    	private final Timestamp min;
    	private final long count;
    	
    	public Range(Timestamp max, Timestamp min, long count) {
    		this.max = max;
    		this.min = min;
    		this.count = count;
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
    	if (getConnection()!=null) {
	        try (PreparedStatement stm = getConnection().prepareStatement("select count(TS), max(TS), min(TS) from " + table + " where TS>=? and TS<=?")) {
				stm.setTimestamp(1, new java.sql.Timestamp(cFrom.getTimeInMillis()));
				stm.setTimestamp(2, new java.sql.Timestamp(cTo.getTimeInMillis()));
				try (ResultSet rs = stm.executeQuery()) {
					if (rs.next()) {
						long count = rs.getLong(1);
						Timestamp tMax = rs.getTimestamp(2);
						Timestamp tMin = rs.getTimestamp(3);
						if (tMax != null && tMin != null) {
							return new Range(tMax, tMin, count);
						}
					}
				}
			}
    	} else {
    		ServerLog.getLogger().warning("Cannot create time range for {" + table + "} because connection is not established!");
    	}
        return null;
    }
    
    public synchronized String backup() throws IOException, InterruptedException {
    	SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        ServerLog.getLogger().info("DB Backup");
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
