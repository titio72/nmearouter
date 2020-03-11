package com.aboni.nmea.router.agent.impl;

import com.aboni.geo.NMEAMWDConverter;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import net.sf.marineapi.nmea.sentence.*;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class NMEAMWDSentenceCalculator extends NMEAAgentImpl {

    private final NMEAMWDConverter conv;
    private long threshold;

    @Inject
    public NMEAMWDSentenceCalculator(@NotNull NMEACache cache) {
        super(cache);
        conv = new NMEAMWDConverter(TalkerId.II);
        setSourceTarget(true, true);
    }

    @Override
    protected final void onSetup(String name, QOS q) {
        if (q != null && q.get("longthreshold")) {
            threshold = 1000;
        } else {
            threshold = -1;
        }
    }

    @Override
    protected void doWithSentence(Sentence s, String source) {
        if (s instanceof HDGSentence) {
            conv.setHeading((HDGSentence) s, getCache().getNow());
        } else if (s instanceof MWVSentence && ((MWVSentence) s).isTrue()) {
            conv.setWind((MWVSentence) s, getCache().getNow());
        } else {
            return;
		}
		MWDSentence outSentence = (threshold==-1)?conv.getMWDSentence():conv.getMWDSentence(threshold);
		if (outSentence!=null) {
			notify(outSentence);
		}
	}

	@Override
	public String getDescription() {
		return "Calculate the absolute wind direction from the true wind and the heading.";
	}
	
	@Override
	public String getType() {
		return "MWD Calculator";
	}

}
