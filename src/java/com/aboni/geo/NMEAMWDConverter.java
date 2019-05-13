package com.aboni.geo;

import com.aboni.misc.Utils;
import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;

public class NMEAMWDConverter {

	private HDGSentence heading;
	private MWVSentence trueWind;
	private long tsHeading;
	private long tsWind;
	private final TalkerId id;
	private static final int THRESHOLD = 300 /*ms*/;
	
	public NMEAMWDConverter(TalkerId id) {
		this.id = id;
	}

    public MWDSentence getMWDSentence() {
        return getMWDSentence(THRESHOLD);
    }
	
    private static double getTrueHeading(HDGSentence h) {
		double dev = 0.0;
		double var = 0.0;
		try { dev = h.getDeviation(); } catch (DataNotAvailableException e) { /* optional data*/ }
		try { var = h.getVariation(); } catch (DataNotAvailableException e) { /* optional data*/ }
		return h.getHeading() + var + dev;
    }
	
    private static double getMagHeading(HDGSentence h) {
		double dev = 0.0;
		try { dev = h.getDeviation(); } catch (DataNotAvailableException e) { /* optional data*/ }
		return h.getHeading() + dev;
    }
    
    /**
     * Calculate the wind direction relative to north and return it in the form of MWDSentence message.
     * The calculation takes into account the age of the wind and heading sample and requires that they are not
     * farther the "threshold" milliseconds. 
     * @param threshold The consistency threshold of wind and heading 
     * @return The MWDSentence containing the wind speed and direction respect to north 
     */
	public MWDSentence getMWDSentence(long threshold) {
		/*
		 * Check if the wind and heading are close enough to make sense summing them up.
		 */
		if (heading!=null && trueWind!=null && Math.abs(tsHeading-tsWind)<threshold/*ms*/) {
			MWDSentence s = (MWDSentence) SentenceFactory.getInstance().createParser(id, SentenceId.MWD);

			double td = getTrueHeading(heading) + trueWind.getAngle();
			td = Utils.normalizeDegrees0To360(td);
			s.setTrueWindDirection(td);

			double md = getMagHeading(heading) + trueWind.getAngle();
			md = Utils.normalizeDegrees0To360(md);
			s.setMagneticWindDirection(md);

			s.setWindSpeedKnots(trueWind.getSpeed());
			s.setWindSpeed(trueWind.getSpeed()*0.51444444444);
			return s;
		} else {
			return null;
		}
	}
	
	/**
	 * Set the heading (necessary to calculate the direction of the wind from north).
	 * The sample will be tagged with a timestamp set to now. 
	 * @param s The HDG sentence used for enriching the wind direction
	 */
	public void setHeading(HDGSentence s) {
		setHeading(s, System.currentTimeMillis());
	}
	
	/**
	 * Set the heading (necessary to calculate the direction of the wind from north).
	 * @param s		The heading
	 * @param time	The time of the heading sample
	 */
	public void setHeading(HDGSentence s, long time) {
		heading = s;
		tsHeading = (s==null)?0:time;
	}
	
	/**
	 * Set the wind information. Only "true" wind will be taken into account while "relative" will be discarded.
	 * The timestamp is as of now.
	 * @param s The wind sentence to be enriched
	 */
	public void setWind(MWVSentence s) {
		setWind(s, System.currentTimeMillis());
	}
	
	/**
	 * Set the wind information. Only "true" wind will be taken into account while "relative" will be discarded.
	 * @param s		The wind info
	 * @param time	The timestamp of the wind sample
	 */
	public void setWind(MWVSentence s, long time) {
		if (s!=null) {
			if (s.isTrue()) {
				trueWind = s;
				tsWind = time;
			}
		} else {
			trueWind = null;
			tsWind = 0;
		}
	}
}
