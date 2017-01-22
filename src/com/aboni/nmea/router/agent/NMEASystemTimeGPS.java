package com.aboni.nmea.router.agent;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.aboni.nmea.router.impl.NMEAAgentImpl;

import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.ZDASentence;
import net.sf.marineapi.nmea.util.Date;
import net.sf.marineapi.nmea.util.Time;

public class NMEASystemTimeGPS extends NMEAAgentImpl {

	private static final long TOLERANCE_MS = 10000;
	
	private boolean timestampset;
	
	public NMEASystemTimeGPS(String name, QOS qos) {
		super(name, qos);
		setSourceTarget(false, true);
	}
	
	@Override
	protected void doWithSentence(Sentence s, NMEAAgent src) {
		if (!timestampset) {
			Time t = null;
			Date d = null;
			if (s instanceof ZDASentence && s.isValid()) {
				ZDASentence zda = (ZDASentence)s;
				d = zda.getDate();
				t = zda.getTime();
			} else if (s instanceof RMCSentence && s.isValid()) {
				RMCSentence r = (RMCSentence)s;
				d = r.getDate();
				t = r.getTime();
			}
			
			if (d!=null && t!=null) {
				Calendar c = getCalendar(t, d);
				Calendar now = Calendar.getInstance();
				long diff = Math.abs(now.getTimeInMillis() - c.getTimeInMillis());
				if (diff > TOLERANCE_MS) {
                    // time skew from GPS is too high - reset time stamp
	                //System.out.println("Setting time " + c.getTime() + " " + c.getTimeZone());
	                getLogger().Info("New Time {" + c + "}");
					doChangeTime(c);
					timestampset = true;
				}
			}
		}
	}

	private static Calendar getCalendar(Time t, Date d) {
        int hh = t.getOffsetHours();
        int hm = t.getOffsetHours();
        DecimalFormat fh = new DecimalFormat("+00");
        DecimalFormat fm = new DecimalFormat("00");
        String h = "GMT" + fh.format(hh) + ":" + fm.format(hm); 
        TimeZone tz = TimeZone.getTimeZone(h);

        Calendar c = Calendar.getInstance(tz);
		c.set(d.getYear(), d.getMonth()-1, d.getDay(), t.getHour(), t.getMinutes(), (int)t.getSeconds());
		c.set(Calendar.MILLISECOND, (int)((t.getSeconds()-(int)t.getSeconds()))*1000);

		return c;
	}

	private void doChangeTime(Calendar c) {
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
