package com.aboni.nmea.router.agent;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.impl.NMEAAgentImpl;

import net.sf.marineapi.nmea.sentence.DateSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TimeSentence;
import net.sf.marineapi.nmea.util.Date;
import net.sf.marineapi.nmea.util.Time;

public class NMEASystemTimeGPS extends NMEAAgentImpl {

	private static final long TOLERANCE_MS = 2000;
	
	private NMEACache cache;
	
	public NMEASystemTimeGPS(NMEACache cache, NMEAStream stream, String name, QOS qos) {
		super(cache, stream, name, qos);
		this.cache = cache;
		setSourceTarget(false, true);
	}

	@Override
    public String getDescription() {
    	return "";
    }
    

	@Override
	protected void doWithSentence(Sentence s, NMEAAgent src) {
		Time t = null;
		Date d = null;
		if (s instanceof DateSentence && s instanceof TimeSentence && s.isValid()) {
			d = ((DateSentence)s).getDate();
			t = ((TimeSentence)s).getTime();
		}
		
		if (d!=null && t!=null) {
			Calendar c = getCalendar(t, d);
			if (checkTolerance(c, Calendar.getInstance())) {
                // time skew from GPS is too high - reset time stamp
                //System.out.println("Setting time " + c.getTime() + " " + c.getTimeZone());
                getLogger().Info("New Time {" + d + " " + t + "}");
				Calendar newC = doChangeTime(c);
				if (checkTolerance(c, newC)) {
					cache.setTimeSynced();
				}
			}
		}
	}

	private boolean checkTolerance(Calendar c, Calendar c0) {
		long diff = Math.abs(c0.getTimeInMillis() - c.getTimeInMillis());
		return (diff > TOLERANCE_MS);
	}
	
	private static DecimalFormat fh = new DecimalFormat("+00");
	private static DecimalFormat fm = new DecimalFormat("00");
	private static Calendar getCalendar(Time t, Date d) {
        int hh = t.getOffsetHours();
        int hm = t.getOffsetHours();
        String h = "GMT" + fh.format(hh) + ":" + fm.format(hm); 
        TimeZone tz = TimeZone.getTimeZone(h);

        Calendar c = Calendar.getInstance(tz);
		c.set(d.getYear(), d.getMonth()-1, d.getDay(), t.getHour(), t.getMinutes(), (int)t.getSeconds());
		c.set(Calendar.MILLISECOND, (int)((t.getSeconds()-(int)t.getSeconds()))*1000);

		return c;
	}

	private Calendar doChangeTime(Calendar c) {
	    DateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
	    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	    try {
	        getLogger().Info("Running {./setGPSTime '" + formatter.format(c.getTime()) + "'}");
	        ProcessBuilder b = new ProcessBuilder("./setGPSTime", formatter.format(c.getTime()));
            Process proc = b.start();
            int retCode = proc.waitFor();
            getLogger().Info("SetTime Return code {" + retCode + "}");
        } catch (Exception e) {
            getLogger().Error("Target {" + getName() + "} cannot set GPS time", e);
        }
        return Calendar.getInstance();
	}

	@Override
	protected boolean onActivate() {
		return true;
	}

	@Override
	protected void onDeactivate() {
	}

    @Override
    public boolean isUserCanStartAndStop() {
    	return false;
    }
}
