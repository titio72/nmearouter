package com.aboni.nmea.router.processors;

import com.aboni.geo.NMEAMagnetic2TrueConverter;
import com.aboni.geo.TrueWind;
import com.aboni.misc.Utils;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.DataStatus;
import net.sf.marineapi.nmea.util.Units;

/**
 * Calc true wind
 * @author aboni
 */
public class NMEAMWVTrue implements NMEAPostProcess {

	public NMEAMWVTrue(boolean useSOG) {
		useRMC = useSOG;
	}

    private static final long AGE_THRESHOLD = 5000;
    
	private double lastTrueHeading;
	private double lastMagHeading;
	private long lastHeadingTime;
	private double lastSpeed;
	private long lastSpeedTime;

	private final boolean useRMC;
	
	@Override
	public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
		try {
			long time = System.currentTimeMillis(); 
			if (sentence instanceof MWVSentence) {
				MWVSentence mwv = (MWVSentence)sentence;
				if (mwv.isTrue()) {
					// skip it (filter out true wind)
					return new Pair<>(Boolean.FALSE, new Sentence[] {});
				} else if ((time-lastSpeedTime)<AGE_THRESHOLD) {
					// calculate true wind
					TrueWind t = new TrueWind(lastSpeed, mwv.getAngle(), mwv.getSpeed());
					MWVSentence mwvTrue = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
					mwvTrue.setAngle(Utils.normalizeDegrees0_360(t.getTrueWindDeg()));
					mwvTrue.setTrue(true);
					mwvTrue.setSpeed(t.getTrueWindSpeed());
					mwvTrue.setSpeedUnit(Units.KNOT);
					mwvTrue.setStatus(DataStatus.ACTIVE);
					
					if ((time-lastHeadingTime)<AGE_THRESHOLD) {
						MWDSentence mwd = (MWDSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWD);
						mwd.setMagneticWindDirection(Utils.normalizeDegrees0_360(lastMagHeading + mwvTrue.getAngle()));
						mwd.setTrueWindDirection(Utils.normalizeDegrees0_360(lastTrueHeading + mwvTrue.getAngle()));
						mwd.setWindSpeed(Math.round(mwvTrue.getSpeed() * 51.4444) / 100.0);
						mwd.setWindSpeedKnots(mwvTrue.getSpeed());
						return new Pair<>(Boolean.TRUE, new Sentence[] {mwvTrue, mwd});
					} else {
						return new Pair<>(Boolean.TRUE, new Sentence[] {mwvTrue});
					}
				}
			} else if (!useRMC && sentence instanceof VHWSentence) {
				lastSpeed = ((VHWSentence)sentence).getSpeedKnots();
				lastSpeedTime = time;
			} else if (useRMC && sentence instanceof RMCSentence) {
				lastSpeed = ((RMCSentence)sentence).getSpeed();
				lastSpeedTime = time;
			} else if (sentence instanceof HDMSentence) {
				HDMSentence hdm = (HDMSentence)sentence;
				lastMagHeading = hdm.getHeading();
				lastTrueHeading = new NMEAMagnetic2TrueConverter().getTrue(lastMagHeading);
				lastHeadingTime = time;
			}
			return null;
		} catch (Exception e) {
            ServerLog.getLogger().warning("Cannot enrich wind message {" + sentence + "} erro {" + e.getLocalizedMessage() + "}");
		}
		return new Pair<>(Boolean.TRUE, null);
	}

	@Override
	public void onTimer() {
		// nothing to do here
	}
	
}
