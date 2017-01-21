package com.aboni.geo;

import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.HDMSentence;
import net.sf.marineapi.nmea.sentence.HDTSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.util.Position;

public class NMEAMagnetic2TrueConverter {

	private TSAGeoMag geo;
	private Position pos;
	
	public NMEAMagnetic2TrueConverter() {
		geo = new TSAGeoMag(ServerLog.getLogger().getBaseLogger());
		pos = new Position(43.0, 10.0);
	}
	
    public double getTrue(double magnetic, Position p) {
        double declination = geo.getDeclination(p.getLatitude(), p.getLongitude(), 2016.5, 0);
        return magnetic + declination;
    }

    public double getMagnetic(double trueCourse, Position p) {
        double declination = geo.getDeclination(p.getLatitude(), p.getLongitude(), 2016.5, 0);
        return trueCourse - declination;
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
		double m_heading = magSentence.getHeading();
		double t_heading = getTrue(m_heading, getPosition());
		if (t_heading<0) t_heading += 360.0; 
		HDTSentence s = (HDTSentence)SentenceFactory.getInstance().createParser(magSentence.getTalkerId(), SentenceId.HDT);
		s.setHeading(t_heading);
		return s;
	}
}
