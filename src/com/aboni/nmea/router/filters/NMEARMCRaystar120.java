package com.aboni.nmea.router.filters;

import java.util.Calendar;
import java.util.TimeZone;

import com.aboni.nmea.router.agent.impl.system.NMEATimestampExtractor;

import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.util.DataStatus;
import net.sf.marineapi.nmea.util.Date;
import net.sf.marineapi.nmea.util.Position;
import net.sf.marineapi.nmea.util.Time;

public class NMEARMCRaystar120 implements NMEAPostProcess {

	private long previousTimeStamp;
	private long base;
	
	private int count;
	private static TimeZone tzUTC = TimeZone.getTimeZone("UTC"); 

	private Position[] positions = new Position[10]; 
	private int pp = -1;
	
	private Position reference;
	
	/**
	 * Calculate the average position of the last 10 samples
	 * @param p	The position of the current sample.
	 * @return the average position
	 */
	private void updateReference(Position p) {
		pp = (pp + 1) % 10;
		positions[pp] = p;
		double lat = 0.0;
		double lon = 0.0;
		int cc = 0;
		for (int i = 0; i<10 && positions[(pp - i) % 10] != null; i++) {
			lat += positions[(pp - i) % 10].getLatitude();
			lon += positions[(pp - i) % 10].getLongitude();
			cc++;
		}
		if (cc==10) {
			reference = new Position(lat / 10.0, lon / 10.0);
		} else {
			reference = null;
		}
	}
	
	private boolean isPositionValid(Position p) {
		if (reference!=null) {
			return reference.distanceTo(p) <= 1.0; /* less than 1 mile from the average of the last points */ 
		} else {
			return false;
		}
	}
	
	@Override
	public Sentence[] process(Sentence sentence, String src) {
		
		if (sentence instanceof RMCSentence) {
			RMCSentence rmc = (RMCSentence)sentence;
			
			updateReference(rmc.getPosition());
			
			Calendar c = NMEATimestampExtractor.getTimestamp(rmc);
			if (c!=null) {
				long t = c.getTimeInMillis();
				if (previousTimeStamp!=0) {
					if ((t-previousTimeStamp)>9500 && (t-previousTimeStamp)<=11000) {
						base = t;
						count = 0;
					} else if ((t-previousTimeStamp)>11000) {
						base = 0;
						count = 0;
					}
				}
				previousTimeStamp = t;
				if (base!=0 && base==t && isPositionValid(rmc.getPosition()) ) {
					long newTimestamp = count * 1000 + c.getTimeInMillis();
					Calendar c1 = Calendar.getInstance(tzUTC);
					c1.setTimeInMillis(newTimestamp);
					Time newTime = new Time(c1.get(Calendar.HOUR_OF_DAY), c1.get(Calendar.MINUTE), c1.get(Calendar.SECOND), 0, 0);
					Date newDate = new Date(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH) + 1, c1.get(Calendar.DAY_OF_MONTH));
					rmc.setTime(newTime);
					rmc.setDate(newDate);
					count++;
				} else {
					rmc.setStatus(DataStatus.VOID);
				}
			}
		}
		return null;
	}

}
