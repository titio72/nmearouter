package com.aboni.nmea.router.filters;

import com.aboni.geo.NMEAMagnetic2TrueConverter;
import com.aboni.geo.Utils;
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

    private NMEAMagnetic2TrueConverter m;
    
	public NMEARMC2VTGProcessor() {
	    m = new NMEAMagnetic2TrueConverter();
	}

	@Override
	public Sentence[] process(Sentence sentence, String src) {
		try {
			
			if (sentence.getSentenceId().equals(SentenceId.RMC.toString())) {
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
				return new Sentence[] {vtg};
			}
		} catch (Exception e) {
			ServerLog.getLogger().Error("Cannot process message!", e);
		}
		return null;
	}
	
}
