package com.aboni.nmea.router.processors;

import com.aboni.utils.Pair;

import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TalkerId;

public class NMEAChangeTalkerProcessor implements NMEAPostProcess {

	private final TalkerId fromTalker;
	private final TalkerId toTalker;
	
	public NMEAChangeTalkerProcessor(TalkerId fromTalker, TalkerId toTalker) {
		this.fromTalker = fromTalker;
		this.toTalker = toTalker;
	}	
	public NMEAChangeTalkerProcessor(TalkerId toTalker) {
		this.fromTalker = null;
		this.toTalker = toTalker;
	}

	@Override
	public Pair<Boolean, Sentence[]> process(Sentence s, String src) {
		if (fromTalker==null|| fromTalker.equals(s.getTalkerId())) {
			s.setTalkerId(toTalker);
		}
		return new Pair<>(Boolean.TRUE, new Sentence[] {s});
	}
	@Override
	public void onTimer() {
	}

}
