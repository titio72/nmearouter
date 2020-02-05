package com.aboni.nmea.router.agent.impl;

import com.aboni.geo.NMEAMWDConverter;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import net.sf.marineapi.nmea.sentence.*;

public class NMEAMWDSentenceCalculator extends NMEAAgentImpl {

	private final NMEAMWDConverter conv;
	private final long threshold;
	
	public NMEAMWDSentenceCalculator(NMEACache cache, String name, QOS qos) {
		super(cache, name, qos);
		conv = new NMEAMWDConverter(TalkerId.II);
        if (qos!=null && qos.get("longthreshold")) {
            threshold = 1000;
        } else {
            threshold = -1;
        }
		setSourceTarget(true, true);
	}

	@Override
	protected void doWithSentence(Sentence s, String source) {
		if (s instanceof HDGSentence) {
            conv.setHeading((HDGSentence) s, getCache().getNow());
        } else if (s instanceof MWVSentence && ((MWVSentence)s).isTrue()) {
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
