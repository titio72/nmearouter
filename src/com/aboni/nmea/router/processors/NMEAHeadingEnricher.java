package com.aboni.nmea.router.processors;

import com.aboni.geo.NMEAMagnetic2TrueConverter;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.utils.DataEvent;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.HDMSentence;
import net.sf.marineapi.nmea.sentence.HDTSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.util.Position;

/**
 * Enrich HDG heading information:
 * 1) Listen to GPS location to set the magnetic variation into the HDG sentence (if not present)
 * 2) Split the sentence in HDM & HDT   
 * @author aboni
 *
 */
public class NMEAHeadingEnricher implements NMEAPostProcess {

    private NMEAMagnetic2TrueConverter m;
    
    private boolean doHDT = true;
    private NMEACache cache;
    
    public NMEAHeadingEnricher(NMEACache cache) {
        m = new NMEAMagnetic2TrueConverter();
        this.cache = cache;
    }
    
    public NMEAHeadingEnricher(NMEACache cache, boolean hdm, boolean hdt) {
        m = new NMEAMagnetic2TrueConverter();
        this.cache = cache;
        this.doHDT = hdt;
    }

    @Override
    public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
        try {
            if (sentence instanceof HDMSentence) {
                HDMSentence hdm = (HDMSentence)sentence;
                HDGSentence hdg = getHDG(hdm);
                boolean canDoT = fillVariation(hdg, getLastPosition());
                if (doHDT && canDoT) {
                	return new Pair<>(Boolean.TRUE, new Sentence[] {hdg, getHDT(hdg)});
                } else {
                	return new Pair<>(Boolean.TRUE, new Sentence[] {hdg});
                }
            }
        } catch (Exception e) {
            ServerLog.getLogger().Warning("Cannot enrich heading process message {" + sentence + "} erro {" + e.getLocalizedMessage() + "}");
        }
        return null;
    }

	private boolean fillVariation(HDGSentence hdg, Position lastPosition) {
		boolean canDoT = false;
		try {
		    hdg.getVariation();
		    canDoT = true;
		} catch (DataNotAvailableException e) {
		    if (lastPosition!=null) {
		        double d = m.getDeclination(lastPosition);
		        d = Utils.normalizeDegrees180_180(d);
		        hdg.setVariation(d);
		        canDoT = true;
		    } else {
		        //hdg.setVariation(0.0);
		    }
		}
		return canDoT;
	}

	private Position getLastPosition() {
		Position lastPosition = null;
		DataEvent<PositionSentence> ev = cache.getLastPosition();
		if (ev!=null && ev.data!=null) {
		    lastPosition = ev.data.getPosition(); 
		}
		return lastPosition;
	}

	private HDGSentence getHDG(HDMSentence hdm) {
		HDGSentence hdg = (HDGSentence) SentenceFactory.getInstance().createParser(hdm.getTalkerId(), SentenceId.HDG);
		hdg.setHeading(hdm.getHeading());
		return hdg;
	}

	private HDTSentence getHDT(HDGSentence hdg) {
		HDTSentence hdt = (HDTSentence) SentenceFactory.getInstance().createParser(hdg.getTalkerId(), SentenceId.HDT);
		double var = 0.0;
		double dev = 0.0;
		try { var = hdg.getVariation(); } catch (Exception e) {}
		try { dev = hdg.getDeviation(); } catch (Exception e) {}
		hdt.setHeading(Utils.normalizeDegrees0_360(hdg.getHeading() + var + dev));
		return hdt;
	}

	@Override
	public void onTimer() {
	}
    
}
