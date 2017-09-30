package com.aboni.nmea.router.batch;


import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.aboni.utils.db.DBHelper;

public class TrackToDB2 {
	
	class PPP {
	    double lat;
	    double lon;
	    Calendar timestamp;
	}

	DBHelper db;
 
    private SimpleDateFormat dfParser;
	
	public TrackToDB2() {
	    dfParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    dfParser.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public void load(String file) throws Exception {
	    int i = 0;
	    db = new DBHelper(false);
	    
	    PreparedStatement st = db.getConnection().prepareStatement("insert into track (lat, lon, TS, anchor, dTime, speed) values (?, ?, ?, ?, ?, ?)");
	    
	    
	    FileReader f = new FileReader(file);
		BufferedReader r = new BufferedReader(f);
		String pos;
		boolean exit = false;
		String lastPos = null;
		PPP last = null;
		while ((pos = r.readLine())!=null && !exit) {
		    System.out.println(pos);
			PPP p;
			try {
				p = getPoint(pos);
				if (p!=null) {
					if (last!=null) {
					    if (p.timestamp.before(last.timestamp)) {
	                        System.out.println(lastPos);
	                        System.out.println(pos);
	                        System.out.println("");
					    }
					}
	
					st.setDouble(1, p.lat);
	                st.setDouble(2, p.lon);
				    Timestamp x = new Timestamp(p.timestamp.getTimeInMillis());
				    st.setTimestamp(3, x);
	                st.setInt(4, 0);
	                st.setInt(5, (last!=null)?(int)(p.timestamp.getTimeInMillis()-last.timestamp.getTimeInMillis()):0);
	                st.setDouble(6, 0.0);
				    st.execute();
	
				    i++;
				    if (i%1000==0) {
				        db.getConnection().commit();
				    }
	
				    last = p;
	                lastPos = pos;
				}
			} catch (Exception e) {
				e.printStackTrace();
                System.in.read();
			}
		}
		//db.getConnection().commit();		
		db.getConnection().close();

		r.close();
		f.close();
	}

	private PPP getPoint(String pos) throws Exception {
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
            e.printStackTrace();
        }
	    
	    
	}
}
