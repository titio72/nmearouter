package com.aboni.nmea.router.filters;

import java.util.Calendar;
import java.util.TimeZone;

import com.aboni.nmea.router.agent.impl.system.NMEATimestampExtractor;

import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.util.DataStatus;
import net.sf.marineapi.nmea.util.Date;
import net.sf.marineapi.nmea.util.Time;

public class NMEARMCRaystar120 implements NMEAPostProcess {

	private long previousTimeStamp;
	private long base;
	
	private long overrideNow;
	
	public void setOverrideTime(long l) {
		overrideNow = l;
	}
	
	private long getNow() {
		return (overrideNow>0?overrideNow:System.currentTimeMillis());
	}
	
	
	@Override
	public Sentence[] process(Sentence sentence, String src) {
		
		if (sentence instanceof RMCSentence) {
			RMCSentence rmc = (RMCSentence)sentence;
			Calendar c = NMEATimestampExtractor.getTimestamp(rmc);
			if (c!=null) {
				long t = c.getTimeInMillis();
				if (previousTimeStamp!=0) {
					if ((t-previousTimeStamp)>9500 && (t-previousTimeStamp)<15000) {
						base = getNow();
					}
				}
				previousTimeStamp = t;
				if (base!=0) {
					long newTimestamp = Math.round((getNow() - base)/1000.0)*1000 + c.getTimeInMillis();
					Calendar c1 = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
					c1.setTimeInMillis(newTimestamp);
					Time newTime = new Time(c1.get(Calendar.HOUR_OF_DAY), c1.get(Calendar.MINUTE), c1.get(Calendar.SECOND), 0, 0);
					Date newDate = new Date(c1.get(Calendar.YEAR), c1.get(Calendar.MONTH) + 1, c1.get(Calendar.DAY_OF_MONTH));
					rmc.setTime(newTime);
					rmc.setDate(newDate);
				} else {
					rmc.setStatus(DataStatus.VOID);
				}
			}
		}
		return null;
	}

}
