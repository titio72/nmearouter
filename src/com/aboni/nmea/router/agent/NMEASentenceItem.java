package com.aboni.nmea.router.agent;

import java.util.StringTokenizer;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEASentenceItem {

	private Sentence sentence;
	private long timestamp;
	private String data;
	
	@Override
	public String toString() {
		return "[" + timestamp + "][" + data + "] " + sentence.toSentence();
	}

	public NMEASentenceItem(Sentence sentence, long timestamp, String data) {
		this.sentence = sentence;
		this.timestamp = timestamp;
		this.data = data;
	}
	
	public NMEASentenceItem(String line) throws Exception {
		StringTokenizer tkz = new StringTokenizer(line, "]");
		String sT = tkz.nextToken().substring(1);
		String sD = tkz.nextToken().substring(1);
		String sS = tkz.nextToken().substring(1);
		sentence = SentenceFactory.getInstance().createParser(sS);
		timestamp = Long.parseLong(sT);
		data = sD;
	}
	
	public Sentence getSentence() {
		return sentence;
	}
	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
}
