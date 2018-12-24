package com.aboni.geo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.util.Position;

public class TrackLoaderFile implements TrackLoader {

	public class MalformedPointLineException extends Exception {

		private static final long serialVersionUID = 1L;

		public MalformedPointLineException(String string, Exception e) {
			super(string, e);
		}

	}

	private final PositionHistory h;
	private final SimpleDateFormat dfParser;
	private final String file;
	
	public TrackLoaderFile(String file) {
	    dfParser = new SimpleDateFormat("ddMMyy HHmmss.SSS");
	    dfParser.setTimeZone(TimeZone.getTimeZone("UTC"));
        h = new PositionHistory(0);
        this.file = file;
	}
	
	@Override
	public boolean load(Calendar from, Calendar to) {
		boolean res = false;
		try {
			FileReader f = new FileReader(file);
			BufferedReader r = new BufferedReader(f);
			String pos;
			boolean exit = false;
			while ((pos = r.readLine())!=null && !exit) {
				GeoPositionT p;
				try {
					p = getPoint(pos);
					exit = p.getTimestamp() > to.getTimeInMillis();
					if (p.getTimestamp() >= from.getTimeInMillis() && !exit) {
							h.addPosition(p);
					}
				} catch (MalformedPointLineException e) {
					ServerLog.getLogger().Debug("Cannot parse line " + pos);
				}
			}
			r.close();
			f.close();
			res = true;
		} catch (Exception e) {
			ServerLog.getLogger().Error("Error loading track", e);
		}
		return res;
	}

	private GeoPositionT getPoint(String pos) throws MalformedPointLineException {
		// 1472189694509 260816 053454.000 42.6809667 N 9.2991000 E
		try {
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

			return new GeoPositionT(t.getTimeInMillis(),
					new Position(
							Double.parseDouble(lat) * ("N".equals(latNS)?1.0:-1.0),
							Double.parseDouble(lon) * ("E".equals(lonEW)?1.0:-1.0)
							));
		} catch (Exception e) {
			throw new MalformedPointLineException("Malformed line " + pos, e);
		}
	}

	public PositionHistory getTrack() {
		return h;
	}
}
