package com.aboni.nmea.router.filters;

import com.aboni.geo.TSAGeoMag;
import com.aboni.geo.Utils;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.HDTSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.util.CompassPoint;
import net.sf.marineapi.nmea.util.Position;

public class NMEAHDM2HDTConverter {

	private TSAGeoMag geo;
	private Position pos;
	
	public NMEAHDM2HDTConverter() {
		geo = new TSAGeoMag();
		pos = new Position(43.0, 10.0);
	}
	
	public double getTrue(double magnetic, Position p) {
		double declination = geo.getDeclination(p.getLatitude(), p.getLongitude());
		return magnetic - declination;
	}

	public void setPosition(Sentence s) {
		if (s instanceof PositionSentence) {
			PositionSentence gll = (PositionSentence)s;
			double lat = gll.getPosition().getLatitude();
			double lon = gll.getPosition().getLatitude();
			if (gll.getPosition().getLatitudeHemisphere()==CompassPoint.SOUTH) lon = -lon;
			pos = new Position(lat, lon);
		}
	}

    public void setPosition(net.sf.marineapi.nmea.util.Position p) {
        double lat = p.getLatitude();
        double lon = p.getLongitude();
        if (p.getLatitudeHemisphere()==CompassPoint.SOUTH) lat = -lat;
        if (p.getLongitudeHemisphere()==CompassPoint.WEST) lon = -lon;
        pos = new Position(lat, lon);    
    }
    
    public HDTSentence getTrueSentence(TalkerId id, double m_heading) {
        double t_heading = getTrue(m_heading, pos);
        HDTSentence s = (HDTSentence)SentenceFactory.getInstance().createParser(id, SentenceId.HDT);
        s.setHeading(Utils.normalizeDegrees0_360(t_heading));
        return s;
    }
    
    public HDGSentence getTrueSentenceX(double m_heading, double variation, TalkerId id) {
        double declination = geo.getDeclination(pos.getLatitude(), pos.getLongitude());
        HDGSentence s = (HDGSentence)SentenceFactory.getInstance().createParser(id, SentenceId.HDG);
        s.setHeading(Utils.normalizeDegrees0_360(m_heading));
        s.setDeviation(Utils.normalizeDegrees180_180(declination));
        s.setVariation(Utils.normalizeDegrees180_180(variation));
        return s;
    }
}
