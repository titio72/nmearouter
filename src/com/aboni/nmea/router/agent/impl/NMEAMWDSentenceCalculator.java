package com.aboni.nmea.router.agent.impl;

import com.aboni.geo.NMEAMWDConverter;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;

import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.MWDSentence;
import net.sf.marineapi.nmea.sentence.MWVSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TalkerId;

public class NMEAMWDSentenceCalculator extends NMEAAgentImpl {

	private NMEAMWDConverter conv;
	private long threshold;
	
	public NMEAMWDSentenceCalculator(NMEACache cache, NMEAStream stream, String name) {
		this(cache, stream, name, null);
	}

	public NMEAMWDSentenceCalculator(NMEACache cache, NMEAStream stream, String name, QOS qos) {
		super(cache, stream, name, qos);
		conv = new NMEAMWDConverter(TalkerId.II);
        if (qos!=null && qos.get("longthreshold")) {
            threshold = 1000;
        } else {
            threshold = -1;
        }
		setSourceTarget(true, true);
	}

	@Override
	protected void doWithSentence(Sentence s, NMEAAgent source) {
		if (s instanceof HDGSentence) {
			conv.setHeading((HDGSentence) s, System.currentTimeMillis());
		} else if (s instanceof MWVSentence && ((MWVSentence)s).isTrue()) {
			conv.setWind((MWVSentence) s, System.currentTimeMillis());
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
		return "";
	}
	

}
