package com.aboni.nmea.router.agent.impl;

import com.aboni.geo.NMEAMWDConverter;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.OnSentence;
import com.aboni.nmea.router.agent.QOS;
import net.sf.marineapi.nmea.sentence.*;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class NMEAMWDSentenceCalculator extends NMEAAgentImpl {

    private final NMEAMWDConverter converter;
    private long threshold;

    @Inject
    public NMEAMWDSentenceCalculator(@NotNull NMEACache cache) {
        super(cache);
        converter = new NMEAMWDConverter(TalkerId.II);
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

    @OnSentence
    public void onSentence(Sentence s, String source) {
        if (s instanceof HDGSentence) {
            converter.setHeading((HDGSentence) s, getCache().getNow());
        } else if (s instanceof MWVSentence && ((MWVSentence) s).isTrue()) {
            converter.setWind((MWVSentence) s, getCache().getNow());
        } else {
            return;
        }
        MWDSentence outSentence = (threshold == -1) ? converter.getMWDSentence() : converter.getMWDSentence(threshold);
        if (outSentence != null) {
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
