package com.aboni.nmea.router.agent.impl.system;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.Sentence;

public class SystemTimeChecker {

	private static final long TOLERANCE_MS = 5000;
	
	private NMEACache cache;
	
	public SystemTimeChecker(NMEACache cache) {
		this.cache = cache;
	}
	public void checkAndSetTime(Sentence s, NMEAAgent src) {
		Calendar c = NMEATimestampExtractor.getTimestamp(s);
		if (c!=null) {
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
