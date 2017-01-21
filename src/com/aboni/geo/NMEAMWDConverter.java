package com.aboni.geo;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.HeadingSentence;
import net.sf.marineapi.nmea.sentence.MWDSentence;
import net.sf.marineapi.nmea.sentence.MWVSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;

public class NMEAMWDConverter {

	private HeadingSentence magHeading;
	private long tsMagHeading;
	
	private MWVSentence trueWind;
	private long tsTrueWind;
	
	private PositionSentence position;
	
	private TalkerId id;
	
	private static final int THRESHOLD = 100 /*ms*/;
	
	public NMEAMWDConverter(TalkerId id) {
		this.id = id;
	}

    public MWDSentence getMWDSentence() {
        return getMWDSentence(THRESHOLD);
    }
	
	public MWDSentence getMWDSentence(long threshold) {
		/*
		 * Check if the wind and heading are close enough to make sense summing them up.
		 */
		if (magHeading!=null && trueWind!=null && Math.abs(tsMagHeading-tsTrueWind)<threshold/*ms*/) {
			MWDSentence s = (MWDSentence) SentenceFactory.getInstance().createParser(id, SentenceId.MWD);
			double d = magHeading.getHeading() + trueWind.getAngle();
			d = Utils.normalizeDegrees0_360(d);
			s.setMagneticWindDirection(d);
			if (position!=null) {
				NMEAMagnetic2TrueConverter m = new NMEAMagnetic2TrueConverter();
				d = m.getTrue(d, position.getPosition());
				d = Utils.normalizeDegrees0_360(d);
				s.setTrueWindDirection(d);
			} else {
				s.setTrueWindDirection(d);
			}
			s.setWindSpeedKnots(trueWind.getSpeed());
			s.setWindSpeed(trueWind.getSpeed()*0.51444444444);
			return s;
		} else {
			return null;
		}
	}
	
	public void setHeading(HeadingSentence s) {
		setHeading(s, System.currentTimeMillis());
	}
	
	public void setHeading(HeadingSentence s, long time) {
		magHeading = s;
		tsMagHeading = (s==null)?0:time;
	}
	
	public void setWind(MWVSentence s) {
		setWind(s, System.currentTimeMillis());
	}
	
	public void setWind(MWVSentence s, long time) {
		if (s!=null) {
			if (s.isTrue()) {
				trueWind = s;
				tsTrueWind = time;
			}
		} else {
			trueWind = null;
			tsTrueWind = 0;
		}
	}
	
	public void setPosition(PositionSentence s) {
		position = s;
	}
}
