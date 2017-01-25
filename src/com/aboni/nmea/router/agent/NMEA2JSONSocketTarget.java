package com.aboni.nmea.router.agent;

import com.aboni.nmea.sentences.NMEA2JSON;

import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEA2JSONSocketTarget extends NMEASocketTarget {

	private NMEA2JSON js;
	
	public NMEA2JSONSocketTarget(String name, int port, QOS q) {
		super(name, port, q);
		setSourceTarget(false, true);
		js = new NMEA2JSON();
	}

	@Override
	protected String getOutSentence(Sentence s) {
		return js.convert(s);
	}
}
