package com.aboni.nmea.router.agent;

import com.aboni.geo.NMEAMWDConverter;
import com.aboni.nmea.router.impl.NMEAAgentImpl;

import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.HDMSentence;
import net.sf.marineapi.nmea.sentence.HeadingSentence;
import net.sf.marineapi.nmea.sentence.MWDSentence;
import net.sf.marineapi.nmea.sentence.MWVSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TalkerId;

public class NMEAMWDSentenceCalculator extends NMEAAgentImpl {

	private NMEAMWDConverter conv;
	private long threshold;
	private boolean hdg;
	
	public NMEAMWDSentenceCalculator(String name) {
		this(name, null);
	}

	public NMEAMWDSentenceCalculator(String name, QOS qos) {
		super(name, qos);
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
			conv.setHeading((HeadingSentence) s, System.currentTimeMillis());
			hdg = true;
		} else if (!hdg && s instanceof HDMSentence) {
			conv.setHeading((HeadingSentence) s, System.currentTimeMillis());
		} else if (s instanceof MWVSentence && ((MWVSentence)s).isTrue()) {
			conv.setWind((MWVSentence) s, System.currentTimeMillis());
		} else if (s instanceof RMCSentence) {
			conv.setPosition((PositionSentence) s);
		} else {
			return;
		}
		MWDSentence outSentence = (threshold==-1)?conv.getMWDSentence():conv.getMWDSentence(threshold);
		if (outSentence!=null) {
			notify(outSentence);
		}
	}

}
