package com.aboni.nmea.router.processors;

import java.util.Calendar;

import com.aboni.geo.NMEAMagnetic2TrueConverter;
import com.aboni.misc.Utils;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.VTGSentence;
import net.sf.marineapi.nmea.util.FaaMode;

/**
 * Used to produce a VTG sentence from a RMC to match requirement of NKE
 * @author aboni
 */
public class NMEARMC2VTGProcessor implements NMEAPostProcess {
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
	
	
    private final NMEAMagnetic2TrueConverter m;
    
	public NMEARMC2VTGProcessor() {
	    this(Calendar.getInstance().get(Calendar.YEAR));
	}

	public NMEARMC2VTGProcessor(double year) {
	    m = new NMEAMagnetic2TrueConverter(year);
	}

	@Override
	public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
		try {
			
			if (sentence instanceof RMCSentence) {
				RMCSentence rmc = (RMCSentence)sentence;
				VTGSentence vtg = (VTGSentence) SentenceFactory.getInstance().createParser(sentence.getTalkerId(), SentenceId.VTG);
				vtg.setMode(FaaMode.AUTOMATIC);
				vtg.setSpeedKnots(rmc.getSpeed());
				vtg.setSpeedKmh(rmc.getSpeed() * 1.852);
				try {
                    vtg.setTrueCourse(rmc.getCourse());
				    m.setPosition(rmc.getPosition());
				    double mag = m.getMagnetic(rmc.getCourse(), rmc.getPosition());
				    mag = Utils.normalizeDegrees0_360(mag);
                    vtg.setMagneticCourse(mag);
				} catch (DataNotAvailableException e) {
				    // stationary, no course (i.e. v=0.0)
	                vtg.setMagneticCourse(0.0);
	                vtg.setTrueCourse(0.0);
				}
				return new Pair<>(Boolean.TRUE, new Sentence[] {vtg});
			}
		} catch (Exception e) {
            ServerLog.getLogger().Warning("Cannot convert message to vtg {" + sentence + "} error {" + e.getMessage() + "}");
		}
		return new Pair<>(Boolean.TRUE, null);
	}

	@Override
	public void onTimer() {
	}
	
}
