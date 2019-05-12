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

public class TrackToDB {
	
	class PPP {
	    double lat;
	    String latE;
	    double lon;
	    String lonE;
	    long t;
	    Calendar timestamp;
	    boolean anchor = false;
	}

    private final SimpleDateFormat dfParser;
	
	public TrackToDB() {
	    dfParser = new SimpleDateFormat("ddMMyy HHmmss.SSS");
	    dfParser.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public void load(String file) throws SQLException, IOException, ClassNotFoundException {
	    int i = 0;
	    try (DBHelper db = new DBHelper(false)) {
			try (FileReader f = new FileReader(file)) {
				try (BufferedReader r = new BufferedReader(f)) {
					try (PreparedStatement st = db.getConnection().prepareStatement("insert into track (lat, lon, TS, anchor) values (?, ?, ?, ?)")) {
						String pos;
						while ((pos = r.readLine()) != null) {
							PPP p;
							try {
								p = getPoint(pos);
								st.setDouble(1, p.lat * (p.latE.equals("N") ? 1.0 : -1.0));
								st.setDouble(2, p.lon * (p.lonE.equals("E") ? 1.0 : -1.0));
								Timestamp x = new Timestamp(p.timestamp.getTimeInMillis());
								st.setTimestamp(3, x);
								st.setInt(4, 0);
								st.execute();
								i++;
								if (i % 1000 == 0) {
									db.getConnection().commit();
								}
							} catch (Exception e) {
								Logger.getGlobal().log(Level.SEVERE, "Error", e);
								System.in.read();
							}
						}
						db.getConnection().commit();
					}
				}
			}
		}
	}

	private PPP getPoint(String pos) throws ParseException {
		// 1472189694509 260816 053454.000 42.6809667 N 9.2991000 E
		String[] tokens = pos.split(" ");
		String date = tokens[1];
		String time = tokens[2];
		String lat = tokens[3];
		String latNS = tokens[4];
		String lon = tokens[5];
		String lonEW = tokens[6];

		Date ts = dfParser.parse(date + " " + time); 
		Calendar t = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		t.setTime(ts);

		PPP p = new PPP();
		p.anchor = false;
		p.lat = Double.parseDouble(lat);
		p.latE = latNS;
		p.lon = Double.parseDouble(lon);
		p.lonE = lonEW;
		p.t = t.getTimeInMillis();
		p.timestamp = t;
		
		return p;
	}
	
	public static void main(String[] args) {
	    TrackToDB tdb = new TrackToDB();
		try {
			tdb.load("track.log");
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "Error", e);
		}
	}
}
