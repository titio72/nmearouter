package com.aboni.nmea.router.processors;

import com.aboni.geo.TrueWind;
import com.aboni.misc.Utils;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.MWVSentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.sentence.VHWSentence;
import net.sf.marineapi.nmea.util.DataStatus;
import net.sf.marineapi.nmea.util.Units;

/**
 * Calc true wind
 * @author aboni
 */
public class NMEAMWVTrue implements NMEAPostProcess {
	
	public NMEAMWVTrue() {
		this(false);
	}

	public NMEAMWVTrue(boolean useSOG) {
		useRMC = useSOG;
	}

    private static final long AGE_THRESHOLD = 5000;
    
	private double lastSpeed;
	private long lastSpeedTime;
	private boolean useRMC;
	
	@Override
	public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
		try {
			
			if (sentence instanceof MWVSentence) {
				MWVSentence mwv = (MWVSentence)sentence;
				if (mwv.isTrue()) {
					// skip it (filter out true wind)
					return new Pair<>(Boolean.FALSE, new Sentence[] {});
				} else if ((System.currentTimeMillis()-lastSpeedTime)<AGE_THRESHOLD) {
					// calculate true wind
					TrueWind t = new TrueWind(lastSpeed, mwv.getAngle(), mwv.getSpeed());
					MWVSentence mwv_t = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
					mwv_t.setAngle(Utils.normalizeDegrees0_360(t.getTrueWindDeg()));
					mwv_t.setTrue(true);
					mwv_t.setSpeed(t.getTrueWindSpeed());
					mwv_t.setSpeedUnit(Units.KNOT);
					mwv_t.setStatus(DataStatus.ACTIVE);
					return new Pair<>(Boolean.TRUE, new Sentence[] {mwv_t});
				}
			} else if (!useRMC && sentence instanceof VHWSentence) {
				lastSpeed = ((VHWSentence)sentence).getSpeedKnots();
				lastSpeedTime = System.currentTimeMillis();
			} else if (useRMC && sentence instanceof RMCSentence) {
				lastSpeed = ((RMCSentence)sentence).getSpeed();
				lastSpeedTime = System.currentTimeMillis();
			}
			return null;
		} catch (Exception e) {
            ServerLog.getLogger().Warning("Cannot enrich wind message {" + sentence + "} erro {" + e.getLocalizedMessage() + "}");
		}
		return new Pair<>(Boolean.TRUE, null);
	}

	@Override
	public void onTimer() {
	}
	
}
