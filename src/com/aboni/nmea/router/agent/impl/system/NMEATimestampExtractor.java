package com.aboni.nmea.router.agent.impl.system;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.ZDASentence;
import net.sf.marineapi.nmea.util.DataStatus;
import net.sf.marineapi.nmea.util.Date;
import net.sf.marineapi.nmea.util.Time;

public class NMEATimestampExtractor {

	private static DecimalFormat fh = new DecimalFormat("+00");
	private static DecimalFormat fm = new DecimalFormat("00");

	private static class DateAndTime {
		Time t = null;
		Date d = null;
	}
	
	private static DateAndTime extractTimestamp(Sentence s) {
		Date d = null;
		Time t = null;
		try {
			if (s instanceof ZDASentence && s.isValid()) {
				ZDASentence zda = (ZDASentence)s;
				d = zda.getDate();
				t = zda.getTime();
			} else if (s instanceof RMCSentence && s.isValid()) {
				RMCSentence r = (RMCSentence)s;
				if (r.getStatus()==DataStatus.ACTIVE) {
					d = r.getDate();
					t = r.getTime();
				}
			}
		} catch (Exception e) {
            ServerLog.getLogger().Error("Error extracting GPS time", e);
		}
		if (d!=null && t!=null) {
			DateAndTime tStamp = new DateAndTime();
			tStamp.t = t;
			tStamp.d = d;
			return tStamp;
		} else {
			return null;
		}
	}
	
	private static Calendar getCalendar(DateAndTime dt) {
        int hh = dt.t.getOffsetHours();
        int hm = dt.t.getOffsetHours();
        String h = "GMT" + fh.format(hh) + ":" + fm.format(hm); 
        TimeZone tz = TimeZone.getTimeZone(h);

        Calendar c = Calendar.getInstance(tz);
		c.set(dt.d.getYear(), dt.d.getMonth()-1, dt.d.getDay(), dt.t.getHour(), dt.t.getMinutes(), (int)dt.t.getSeconds());
		c.set(Calendar.MILLISECOND, (int)((dt.t.getSeconds()-(int)dt.t.getSeconds()))*1000);

		return c;
	}

	public static Calendar getTimestamp(Sentence s) {
		DateAndTime dt = extractTimestamp(s);
		if (dt!=null) {
			return getCalendar(dt);
		} else {
			return null;
		}
	}
}
