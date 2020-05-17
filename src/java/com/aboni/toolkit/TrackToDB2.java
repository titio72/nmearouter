package com.aboni.toolkit;


import com.aboni.utils.db.DBHelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrackToDB2 {

    static class PPP {
	    double lat;
	    double lon;
	    Calendar timestamp;
	}

	DBHelper db;
 
    private final SimpleDateFormat dfParser;
	
	public TrackToDB2() {
	    dfParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    dfParser.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public void load(String file) throws SQLException, IOException, ClassNotFoundException {
	    int i = 0;
	    db = new DBHelper(false);
	    try (PreparedStatement st = db.getConnection().prepareStatement("insert into track (lat, lon, TS, anchor, dTime, speed) values (?, ?, ?, ?, ?, ?)")) {
			try (FileReader f = new FileReader(file)) {
				try (BufferedReader r = new BufferedReader(f)) {
					String pos;
					PPP last = null;
					while ((pos = r.readLine()) != null) {
						Logger.getGlobal().info(pos);
						PPP p;
						try {
							p = getPoint(pos);
							if (p != null) {
								st.setDouble(1, p.lat);
								st.setDouble(2, p.lon);
								Timestamp x = new Timestamp(p.timestamp.getTimeInMillis());
								st.setTimestamp(3, x);
								st.setInt(4, 0);
								st.setInt(5, (last != null) ? (int) (p.timestamp.getTimeInMillis() - last.timestamp.getTimeInMillis()) : 0);
								st.setDouble(6, 0.0);
								st.execute();
								i++;
								if (i % 1000 == 0) {
									db.getConnection().commit();
								}
								last = p;
							}
						} catch (Exception e) {
                            Logger.getGlobal().log(Level.SEVERE, "Error", e);
                            //noinspection ResultOfMethodCallIgnored
                            System.in.read();
                        }
					}
				}
			}
		}
		db.getConnection().commit();
		db.getConnection().close();

	}

	private PPP getPoint(String pos) throws ParseException {
		// 1472189694509 260816 053454.000 42.6809667 N 9.2991000 E
		String[] tokens = pos.split(",");
		if (tokens.length==3) {
			String lat = tokens[0];
			String lon = tokens[1];
	        String tst = tokens[2];
	
	        dfParser.setTimeZone(TimeZone.getTimeZone("UTC"));
			Date ts = dfParser.parse(tst); 
			Calendar t = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			t.setTime(ts);
	
			PPP p = new PPP();
			p.lat = Double.parseDouble(lat);
			p.lon = Double.parseDouble(lon);
			p.timestamp = t;
			return p;
		} else {
			return null;
		}
	}
	
	public static void main(String[] args) {
	    TrackToDB2 tdb = new TrackToDB2();
	    try {
            tdb.load("20161113.csv");
        } catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "Error", e);
        }
	    
	    
	}
}
