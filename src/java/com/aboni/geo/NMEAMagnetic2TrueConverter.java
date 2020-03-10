package com.aboni.geo;

import com.aboni.misc.Utils;
import com.aboni.utils.Constants;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.Position;

import java.time.OffsetDateTime;

public class NMEAMagnetic2TrueConverter {

	private final TSAGeoMag geo;
	private Position pos;
	private final double year;

	public NMEAMagnetic2TrueConverter() {
		geo = new TSAGeoMag(Constants.WMM, ServerLog.getLoggerAdmin().getBaseLogger());
		pos = new Position(43.0, 10.0);

        OffsetDateTime odt = OffsetDateTime.now();
        this.year = (double) odt.getYear() + ((double) odt.getMonthValue() / 12.0);
    }
	
	public NMEAMagnetic2TrueConverter(double year) {
		geo = new TSAGeoMag(Constants.WMM, ServerLog.getLoggerAdmin().getBaseLogger());
		pos = new Position(43.0, 10.0);
		this.year = year;
	}
	
    public double getTrue(double magnetic) {
        double declination = geo.getDeclination(pos.getLatitude(), pos.getLongitude(), year, 0);
        return Utils.normalizeDegrees0To360(magnetic + declination);
    }

    public double getTrue(double magnetic, Position p) {
        double declination = geo.getDeclination(p.getLatitude(), p.getLongitude(), year, 0);
        return Utils.normalizeDegrees0To360(magnetic + declination);
    }

    public double getMagnetic(double trueCourse, Position p) {
        double declination = geo.getDeclination(p.getLatitude(), p.getLongitude(), year, 0);
        return Utils.normalizeDegrees0To360(trueCourse - declination);
    }

    public double getDeclination(Position p) {
        return geo.getDeclination(p.getLatitude(), p.getLongitude(), 2016.5, 0);
    }

	public Position getPosition() {
		return pos;
	}

    public void setPosition(PositionSentence s) {
        setPosition(s.getPosition());
    }

    public void setPosition(Position s) {
        pos = s;
    }
	
	public HDTSentence getTrueSentence(HDMSentence magSentence) {
		return getTrueSentence(magSentence.getTalkerId(), magSentence.getHeading());
	}
	
	public HDTSentence getTrueSentence(TalkerId tid, double magBearing) {
		double trueheading = getTrue(magBearing, getPosition());
		HDTSentence s = (HDTSentence)SentenceFactory.getInstance().createParser(tid, SentenceId.HDT);
		s.setHeading(trueheading);
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
