package com.aboni.nmea.router.processors;

import com.aboni.geo.NMEAMagnetic2TrueConverter;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.utils.DataEvent;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Enrich HDG heading information:
 * 1) Listen to GPS location to set the magnetic variation into the HDG sentence (if not present)
 * 2) Split the sentence in HDM & HDT   
 * @author aboni
 *
 */
public class NMEAHDGEnricher implements NMEAPostProcess {

    private final NMEAMagnetic2TrueConverter m;
    
    private final boolean doHDM;
    private final boolean doHDT;
    private final NMEACache cache;
    
    public NMEAHDGEnricher(NMEACache cache) {
        this(cache, true, true);
    }

    public NMEAHDGEnricher(NMEACache cache, boolean hdm, boolean hdt) {
        m = new NMEAMagnetic2TrueConverter();
        this.cache = cache;
        this.doHDM = hdm;
        this.doHDT = hdt;
    }

    @Override
    public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
        try {
            if (sentence instanceof HDGSentence) {
                HDGSentence hdg = (HDGSentence)sentence;
                List<Sentence> out = new ArrayList<>(2);
                boolean canDoT = fillVariation(hdg, getLastPosition());
                if (doHDM) {
                    out.add(getHDM(hdg));
                }
                if (doHDT && canDoT) {
                    out.add(getHDT(hdg));
                }

                return new Pair<>(Boolean.TRUE, out.toArray(new Sentence[0]));
            } else if ((doHDM && sentence instanceof HDMSentence) || (doHDT && sentence instanceof HDGSentence)) {
            	// skip HDT & HDM if they are supposed to be produced by the enricher
				return new Pair<>(Boolean.FALSE, new Sentence[]{});
			}
        } catch (Exception e) {
            ServerLog.getLogger().warning("Cannot enrich heading process message {" + sentence + "} erro {" + e.getLocalizedMessage() + "}");
        }
        return new Pair<>(Boolean.TRUE, null);
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
		    }
		}
		return canDoT;
	}

	private Position getLastPosition() {
		Position lastPosition = null;
		DataEvent<PositionSentence> ev = cache.getLastPosition();
		if (ev!=null && ev.getData()!=null) {
		    lastPosition = ev.getData().getPosition();
		}
		return lastPosition;
	}

	private HDMSentence getHDM(HDGSentence hdg) {
		HDMSentence hdm = (HDMSentence) SentenceFactory.getInstance().createParser(hdg.getTalkerId(), SentenceId.HDM);
		hdm.setHeading(hdg.getHeading());
		return hdm;
	}

	private HDTSentence getHDT(HDGSentence hdg) {
		HDTSentence hdt = (HDTSentence) SentenceFactory.getInstance().createParser(hdg.getTalkerId(), SentenceId.HDT);
		double var;
		double dev;
		try { var = hdg.getVariation(); } catch (DataNotAvailableException e) { var = 0.0; }
		try { dev = hdg.getDeviation(); } catch (DataNotAvailableException e) { dev = 0.0; }
		hdt.setHeading(Utils.normalizeDegrees0_360(hdg.getHeading() + var + dev));
		return hdt;
	}

	@Override
	public void onTimer() {
		// nothing to do
    }
    
}
