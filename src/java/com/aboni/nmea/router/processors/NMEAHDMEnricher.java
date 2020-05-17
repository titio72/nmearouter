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

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

/**
 * To be used when there's only HDM in the stream and one needs to enrich it with variation and deviation.
 * Enrich HDM heading information and produces additional HDT & HDG sentences
 *
 * @author aboni
 */
public class NMEAHDMEnricher implements NMEAPostProcess {

    private final NMEAMagnetic2TrueConverter m;

    private final NMEACache cache;

    @Inject
    public NMEAHDMEnricher(@NotNull NMEACache cache) {
        m = new NMEAMagnetic2TrueConverter();
        this.cache = cache;
    }

    @Override
    public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
        try {
            if (sentence instanceof HDMSentence) {
                HDMSentence hdm = (HDMSentence) sentence;
                HDGSentence hdg = getHDG(hdm);
				if (fillVariation(hdg, getLastPosition())) {
                	return new Pair<>(Boolean.TRUE, new Sentence[] {hdg, getHDT(hdg)});
                } else {
                	return new Pair<>(Boolean.TRUE, new Sentence[] {hdg});
                }
            } else if (sentence instanceof HDGSentence || sentence instanceof HDTSentence) {
            	// skip HDG and HDT as they are produced by the enricher
				return new Pair<>(Boolean.FALSE, new Sentence[] {});
			}
        } catch (Exception e) {
            ServerLog.getLogger().warning("Cannot enrich heading process message {" + sentence + "} error {" + e.getLocalizedMessage() + "}");
        }
        return null;
    }

	private boolean fillVariation(HDGSentence hdg, Position lastPosition) {
		boolean variationAvailable = false;
		try {
            // if the variation is already present skip the enrichment
            hdg.getVariation();
            variationAvailable = true;
        } catch (DataNotAvailableException e) {
		    if (lastPosition!=null) {
		        double d = m.getDeclination(lastPosition);
		        d = Utils.normalizeDegrees180To180(d);
		        hdg.setVariation(d);
		        variationAvailable = true;
		    }
		}
		return variationAvailable;
	}

	private Position getLastPosition() {
		Position lastPosition = null;
		DataEvent<PositionSentence> ev = cache.getLastPosition();
		if (ev!=null && ev.getData()!=null) {
		    lastPosition = ev.getData().getPosition();
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
		double var;
		double dev;
		try { var = hdg.getVariation(); } catch (DataNotAvailableException e) { var = 0.0; }
		try { dev = hdg.getDeviation(); } catch (DataNotAvailableException e) { dev = 0.0; }
		hdt.setHeading(Utils.normalizeDegrees0To360(hdg.getHeading() + var + dev));
		return hdt;
	}

	@Override
	public void onTimer() {
    	// nothing ot do
	}
    
}
