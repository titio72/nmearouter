package com.aboni.nmea.router.filters;

import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TalkerId;

public class NMEAChangeTalkerProcessor implements NMEAPostProcess {

	private TalkerId fromTalker;
	private TalkerId toTalker;
	
	public NMEAChangeTalkerProcessor(TalkerId fromTalker, TalkerId toTalker) {
		this.fromTalker = fromTalker;
		this.toTalker = toTalker;
	}	
	public NMEAChangeTalkerProcessor(TalkerId toTalker) {
		this.fromTalker = null;
		this.toTalker = toTalker;
	}

	@Override
	public Sentence[] process(Sentence s, String src) {
		if (fromTalker==null|| fromTalker.equals(s.getTalkerId())) {
			s.setTalkerId(toTalker);
		}
		return new Sentence[] {s};
	}

}
