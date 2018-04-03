package com.aboni.nmea.router.agent.impl.system;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.ZDASentence;
import net.sf.marineapi.nmea.util.Date;
import net.sf.marineapi.nmea.util.Time;

public class SystemTimeChecker {

	private static final long TOLERANCE_MS = 5000;
	
	private NMEACache cache;
	
	public SystemTimeChecker(NMEACache cache) {
		this.cache = cache;
	}

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
				d = r.getDate();
				t = r.getTime();
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
	
	public void checkAndSetTime(Sentence s, NMEAAgent src) {
		DateAndTime tStamp = extractTimestamp(s);
		if (tStamp!=null) {
			Calendar c = getCalendar(tStamp);
			Calendar now = Calendar.getInstance();
			if (!checkTimeSkew(now, c)) {
                // time skew from GPS is too high - reset time stamp
                ServerLog.getLogger().Info("Changing system time to {" + c + "}");
				doChangeTime(c);
			}
			if (checkTimeSkew(now, c)) cache.setTimeSynced();
		}
	}
	
	private static boolean checkTimeSkew(Calendar now, Calendar gpstime) {
		long diff = Math.abs(now.getTimeInMillis() - gpstime.getTimeInMillis());
		return diff<TOLERANCE_MS;
	}
	
	private static DecimalFormat fh = new DecimalFormat("+00");
	private static DecimalFormat fm = new DecimalFormat("00");
	
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

	private static void doChangeTime(Calendar c) {
	    DateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
	    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	    try {
	        ServerLog.getLogger().Info("Running {./setGPSTime '" + formatter.format(c.getTime()) + "'}");
	        ProcessBuilder b = new ProcessBuilder("./setGPSTime", formatter.format(c.getTime()));
            Process proc = b.start();
            int retCode = proc.waitFor();
            ServerLog.getLogger().Info("SetTime Return code {" + retCode + "}");
        } catch (Exception e) {
        	ServerLog.getLogger().Error("Cannot set GPS time", e);
        }
	}
}
