package com.aboni.geo;

import java.util.Calendar;

import com.aboni.misc.Utils;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.HDMSentence;
import net.sf.marineapi.nmea.sentence.HDTSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.util.Position;

public class NMEAMagnetic2TrueConverter {

	private TSAGeoMag geo;
	private Position pos;
	private double year;

	public NMEAMagnetic2TrueConverter() {
		geo = new TSAGeoMag(ServerLog.getLogger().getBaseLogger());
		pos = new Position(43.0, 10.0);

		Calendar c = Calendar.getInstance();
		this.year = ((double)c.get(Calendar.YEAR) + (double)(c.get(Calendar.MONTH) + 1) / 12.0); 
		
	}
	
	public NMEAMagnetic2TrueConverter(double year) {
		geo = new TSAGeoMag(ServerLog.getLogger().getBaseLogger());
		pos = new Position(43.0, 10.0);
		this.year = year;
	}
	
    public double getTrue(double magnetic) {
        double declination = geo.getDeclination(pos.getLatitude(), pos.getLongitude(), year, 0);
        return Utils.normalizeDegrees0_360(magnetic + declination);
    }

    public double getTrue(double magnetic, Position p) {
        double declination = geo.getDeclination(p.getLatitude(), p.getLongitude(), year, 0);
        return Utils.normalizeDegrees0_360(magnetic + declination);
    }

    public double getMagnetic(double trueCourse, Position p) {
        double declination = geo.getDeclination(p.getLatitude(), p.getLongitude(), year, 0);
        return Utils.normalizeDegrees0_360(trueCourse - declination);
    }

    public double getDeclination(Position p) {
        return geo.getDeclination(p.getLatitude(), p.getLongitude(), 2016.5, 0);
    }

	public Position getPosition() {
		return pos;
	}

    public void setPosition(PositionSentence s) {
        PositionSentence gll = (PositionSentence)s;
        setPosition(gll.getPosition());
    }

    public void setPosition(Position s) {
        pos = s;
    }
	
	public HDTSentence getTrueSentence(HDMSentence magSentence) {
		return getTrueSentence(magSentence.getTalkerId(), magSentence.getHeading());
	}
	
	public HDTSentence getTrueSentence(TalkerId tid, double bearing) {
		double m_heading = bearing;
		double t_heading = getTrue(m_heading, getPosition());
		HDTSentence s = (HDTSentence)SentenceFactory.getInstance().createParser(tid, SentenceId.HDT);
		s.setHeading(t_heading);
		return s;
	}
	
	public HDGSentence getSentence(TalkerId tid, double bearing, double deviation) {
		HDGSentence s = (HDGSentence)SentenceFactory.getInstance().createParser(tid, SentenceId.HDG);
		s.setHeading(bearing);
		s.setDeviation(deviation);
		s.setVariation(getDeclination(getPosition()));
		return s;
	}
	
}
