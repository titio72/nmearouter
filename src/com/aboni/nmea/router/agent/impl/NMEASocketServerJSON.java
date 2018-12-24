package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;

import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.sentences.NMEA2JSONb;

import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEASocketServerJSON extends NMEASocketServer {

	private final NMEA2JSONb js;
	
	@Override
	public String getType() {
		return "TCP Json Server";
	}
	
	public NMEASocketServerJSON(NMEACache cache, String name, int port, QOS q) {
		super(cache, name, port, q);
		setSourceTarget(true, true);
		js = new NMEA2JSONb();
	}

	@Override
	protected String getOutSentence(Sentence s) {
		return js.convert(s).toString();
	}
}
