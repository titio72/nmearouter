package com.aboni.nmea.router.filters;

import com.aboni.geo.GeoPositionT;
import com.aboni.geo.NMEAMagnetic2TrueConverter;
import com.aboni.misc.Utils;
import com.aboni.utils.ServerLog;

import br.uel.cross.filter.GpsKalmanFilter;
import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.VTGSentence;
import net.sf.marineapi.nmea.util.FaaMode;
import net.sf.marineapi.nmea.util.Position;

/**
 * Used to produce a VTG sentence from a RMC to match requirement of NKE
 * @author aboni
 */
public class NMEAKalmanFilter implements NMEAPostProcess {
	/*
	RMC Recommended Minimum Navigation Information
	 12
	 1 2 3 4 5 6 7 8 9 10 11|
	 | | | | | | | | | | | |
	$--RMC,hhmmss.ss,A,llll.ll,a,yyyyy.yy,a,x.x,x.x,xxxx,x.x,a*hh
	 1) Time (UTC)
	 2) Status, V = Navigation receiver warning
	 3) Latitude
	 4) N or S
	 5) Longitude
	 6) E or W
	 7) Speed over ground, knots
	 8) Track made good, degrees true
	 9) Date, ddmmyy
	10) Magnetic Variation, degrees
	11) E or W
	12) Checksum
	*/
	
	private GpsKalmanFilter filter;

    private NMEAMagnetic2TrueConverter m;

    private void initFilter() {
    	
    }
    
	public NMEAKalmanFilter() {
	    m = new NMEAMagnetic2TrueConverter();
	    filter = new GpsKalmanFilter(1.0);
	}

	public NMEAKalmanFilter(double year) {
	    m = new NMEAMagnetic2TrueConverter(year);
	    filter = new GpsKalmanFilter(1.0);
	}

	private long lastTimeStamp = 0;
	private Position lastPosition = null;
	
	@Override
	public Sentence[] process(Sentence sentence, String src) {
		try {
			if (sentence.getSentenceId().equals(SentenceId.RMC.toString())) {
				RMCSentence rmc = (RMCSentence)sentence;
				Position p = rmc.getPosition();
				long now = System.currentTimeMillis();
				filter.updateVelocity2d(p.getLatitude(), p.getLongitude(), (now-lastTimeStamp)/1000 );
				Position filteredP = new Position(filter.getPosition().getLat(), filter.getPosition().getLng());
				rmc.setPosition(filteredP);
				
				if (lastPosition!=null) {
					GeoPositionT p0 = new GeoPositionT(lastTimeStamp, lastPosition);
					GeoPositionT p1 = new GeoPositionT(now, filteredP);
					double d = p1.distanceTo(p0);
					double sog = d / ((now - lastTimeStamp) / 1000.0 / 60.0 / 60.0);
					rmc.setSpeed(filter.getKmh() / 1.852);
				} else {
					// do nothing
				}
				lastPosition = filteredP;
				lastTimeStamp = now;
				return new Sentence[] {sentence};
			}
		} catch (Exception e) {
			ServerLog.getLogger().Error("Cannot process message!", e);
		}
		return null;
	}
	
}