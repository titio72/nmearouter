package com.aboni.nmea.router.filters;

import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TalkerId;

public class NMEABasicSentenceFilter implements NMEASentenceFilter {

	private String sentenceId;
	private TalkerId talkerId;
	private String source;
	
	public NMEABasicSentenceFilter() {
		sentenceId = "";
		talkerId = null;
		source = "";
	}
	
	public NMEABasicSentenceFilter(String sentenceId, TalkerId talkerId, String source) {
		if (sentenceId==null) sentenceId = "";
		this.sentenceId = sentenceId;
		this.talkerId = talkerId;
		this.source = source;
	}

	public NMEABasicSentenceFilter(String sentenceId, TalkerId talkerId) {
		if (sentenceId==null) sentenceId = "";
		this.sentenceId = sentenceId;
		this.talkerId = talkerId;
		this.source = "";
	}
	
	public NMEABasicSentenceFilter(String sentenceId, String source) {
		if (sentenceId==null) sentenceId = "";
		this.sentenceId = sentenceId;
		this.talkerId = null;
		this.source = source;
	}
	
	public NMEABasicSentenceFilter(String sentenceId) {
		if (sentenceId==null) sentenceId = "";
		this.sentenceId = sentenceId;
		this.talkerId = null;
		this.source = "";
	}
	
	public TalkerId getTalkerId() {
		return talkerId;
	}
	
	public String getSentenceId() {
		return sentenceId;
	}
	
	public String getSource() {
		return source;
	}
	
	private boolean isAllSentences() {
		return sentenceId.isEmpty();
	}
	
	private boolean isAllTalkers() {
		return talkerId==null;
	}
	
	private boolean isAllSources() {
		return source.isEmpty();
	}
	
	@Override
	public boolean match(Sentence s, String src) {
		if (isAllSources() || getSource().equals(src)) {
			if (isAllSentences() || getSentenceId().equals(s.getSentenceId())) {
				if (isAllTalkers() || getTalkerId().equals(s.getTalkerId())) {
					return true;
				}
			}
		}
		return false;
	}
}
